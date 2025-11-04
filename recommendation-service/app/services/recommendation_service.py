import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from scipy.sparse import csr_matrix
from sklearn.neighbors import NearestNeighbors
from fastapi import HTTPException
from typing import List, Dict
from app.repositories.product_repository import fetch_products
from app.repositories.interaction_repository import fetch_user_interactions
from app.repositories.config_repository import fetch_recommendation_config

class HybridRecommendationEngine:
    # Khởi tạo các biến cần thiết
    def __init__(self):
        self.products_df = None # DataFrame sản phẩm
        self.interactions_df = None # DataFrame tương tác người dùng
        self.tfidf_matrix = None # Ma trận TF-IDF cho content-based
        self.vectorizer = None # TF-IDF Vectorizer
        self.user_item_matrix = None # Ma trận user-item cho collaborative filtering
        self.knn_model = None # Mô hình KNN cho collaborative filtering
        
        # Cấu hình mặc định
        self.content_weight = 0.4
        self.collaborative_weight = 0.6
        self.min_interaction_count = 2
        
        # Trọng số cho các loại tương tác
        self.interaction_weights = {
            'VIEW': 1.0,
            'ADD_TO_CART': 2.0,
            'PURCHASE': 5.0,
            'REVIEW': 3.0
        }

    def load_config(self):
        """Đọc cấu hình mô hình từ DB"""
        try:
            print("Reloading config from DB...")
            config = fetch_recommendation_config()
            print(f"Loaded config: {config}")
            self.content_weight = float(config.get('content_weight', 0.4))
            self.collaborative_weight = float(config.get('collaborative_weight', 0.6))
            self.min_interaction_count = int(config.get('min_interaction_count', 2))

            print(f"Loaded config: content_weight={self.content_weight}, "
                  f"collab_weight={self.collaborative_weight}, "
                  f"min_interaction={self.min_interaction_count}")
        except Exception as e:
            print("Could not load config from DB, using defaults:", e)

    # Tải dữ liệu sản phẩm và tương tác
    def load_data(self):
        """Load dữ liệu sản phẩm và tương tác"""
        
        # Load config trước khi train mô hình
        self.load_config()
        
        self.products_df = fetch_products()  # Đổ vào self.products_df
        self.interactions_df = fetch_user_interactions() # Đổ vào self.interactions_df
        
        # Content-based features
        self._prepare_content_features() # Build TF-IDF matrix
        
        # Collaborative filtering features
        if not self.interactions_df.empty:
            self._prepare_collaborative_features() # Build CF model

    # Chuẩn bị features cho Content-based
    def _prepare_content_features(self):
        """Chuẩn bị features cho Content-based"""
        self.products_df['content_features'] = (
            self.products_df['name'].fillna('') + ' ' +
            self.products_df['description'].fillna('') + ' ' +
            self.products_df['material'].fillna('') + ' ' +
            self.products_df['category_name'].fillna('') + ' ' +
            self.products_df['color'].fillna('')
        )
        
        self.vectorizer = TfidfVectorizer(max_features=5000, ngram_range=(1, 2))
        self.tfidf_matrix = self.vectorizer.fit_transform(
            self.products_df['content_features']
        )

    # Chuẩn bị features cho Collaborative Filtering
    def _prepare_collaborative_features(self):
        """Chuẩn bị User-Item Matrix cho Collaborative Filtering"""
        # Tính điểm tương tác
        self.interactions_df['score'] = self.interactions_df.apply(
            lambda row: (
                self.interaction_weights.get(row['interaction_type'], 1.0) * 
                row['interaction_count'] *
                (row['rating'] / 5.0 if pd.notna(row['rating']) else 1.0)
            ),
            axis=1
        )
        
        # Tạo pivot table: users x products
        user_item_df = self.interactions_df.pivot_table(
            index='user_id',
            columns='product_id',
            values='score',
            aggfunc='sum',
            fill_value=0
        )
        
        # Chuyển sang sparse matrix
        self.user_item_matrix = csr_matrix(user_item_df.values)
        
        self.user_ids = user_item_df.index.tolist()
        self.product_ids = user_item_df.columns.tolist()
        
        # Đếm tổng số người dùng đang có trong dữ liệu
        n_users = user_item_df.shape[0]
        
        # Nếu không đủ người dùng, bỏ qua CF
        if n_users < 2:
            # không đủ dữ liệu để CF
            self.user_item_matrix = None
            self.knn_model = None
            return
        
        n_neighbors = min(20, n_users)
        
        # Train KNN model
        self.knn_model = NearestNeighbors(
            metric='cosine', # Dùng cosine similarity
            algorithm='brute', # Dùng brute-force
            n_neighbors=n_neighbors
        )
        
        # Fit mô hình với ma trận user-item
        self.knn_model.fit(self.user_item_matrix)

    # Chuẩn hoá điểm similarity_score
    def normalize_recommendations(self, recs: List[Dict]) -> List[Dict]:
            """Chuẩn hoá similarity_score về [0,1]"""
            if not recs:
                return recs

            scores = [r['similarity_score'] for r in recs]
            min_score, max_score = min(scores), max(scores)

            if max_score == min_score:
                # tránh chia cho 0
                for r in recs:
                    r['similarity_score'] = 1.0
                return recs

            for r in recs:
                r['similarity_score'] = (r['similarity_score'] - min_score) / (max_score - min_score)
            return recs

    # Gợi ý dựa trên Content-based Filtering
    def get_content_based_recommendations(
        self, 
        product_id: int, 
        num_recommendations: int
    ) -> List[Dict]:
        """Gợi ý dựa trên nội dung sản phẩm"""
        
        # Xác định index của sản phẩm đầu vào lấy chỉ số hàng
        product_indices = self.products_df[self.products_df['id'] == product_id].index
        
        print("Gợi ý dựa trên nội dung sản phẩm cho product_id:", product_id)
        
        # Nếu không tìm thấy sản phẩm này trong DataFrame sản phẩm thì trả về lỗi 404
        if product_indices.empty:
            raise HTTPException(
                status_code=404, 
                detail=f"Product {product_id} not found"
            )
        
        # Vị trí hàng của sản phẩm trong ma trận TF-IDF
        product_idx = product_indices[0]
        
        # Tính độ tương đồng cosine giữa sản phẩm gốc và tất cả sản phẩm
        cosine_similarities = cosine_similarity(
            self.tfidf_matrix[product_idx:product_idx+1], 
            self.tfidf_matrix
        ).flatten()
        
        # Lấy các chỉ số của sản phẩm tương tự, bỏ qua chính nó
        similar_indices = cosine_similarities.argsort()[::-1][1:num_recommendations+1]
        
        recommendations = []
        
        # Tạo danh sách gợi ý
        for idx in similar_indices:
            product = self.products_df.iloc[idx]
            recommendations.append({
                'product_id': int(product['id']),
                'similarity_score': float(cosine_similarities[idx]),
                'name': product['name'],
                'category': product['category_name'],
                'price': float(product['price']) if product['price'] else None,
                'recommendation_type': 'CONTENT'
            })
        
        # In ra thông tin gợi ý
        return self.normalize_recommendations(recommendations)

    # Gợi ý dựa trên Collaborative Filtering
    def get_collaborative_recommendations(
        self, 
        user_id: int, 
        num_recommendations: int,
        exclude_items: set = None
    ) -> List[Dict]:
        """Gợi ý dựa trên hành vi người dùng tương tự"""
        
        # Kiểm tra nếu user_item_matrix chưa được tạo thì trả về rỗng
        if self.user_item_matrix is None:
            return []
        
        try:
            user_idx = self.user_ids.index(user_id)
        except ValueError:
            # Người dùng mới chưa có tương tác
            return []
        
        if self.knn_model is None or self.user_item_matrix is None:
            return []
        
        # Lấy tổng số người dùng đã có trong ma trận
        n_users = self.user_item_matrix.shape[0]
        
        # Chọn k láng giềng
        k = min(11, n_users)
        
        # Nếu không đủ người dùng để tìm láng giềng thì trả về rỗng
        if k < 2:
            return [] 
        
        # Tìm người dùng tương tự
        distances, indices = self.knn_model.kneighbors(
            self.user_item_matrix[user_idx],
            n_neighbors=k
        )
        
        print("Checking product IDs overlap...")
        print("Products in interactions:", self.interactions_df['product_id'].unique())
        print("Products in products_df:", self.products_df['id'].unique())
        
        # Lấy sản phẩm từ người dùng tương tự
        similar_users_indices = indices.flatten()[1:]
        user_products = set(
            self.interactions_df[
                self.interactions_df['user_id'] == user_id
            ]['product_id'].tolist()
        )
        
        product_scores = {}
        for similar_user_idx in similar_users_indices:
            similar_user_id = self.user_ids[similar_user_idx]
            similar_user_products = self.interactions_df[
                self.interactions_df['user_id'] == similar_user_id
            ]
            
            for _, row in similar_user_products.iterrows():
                prod_id = row['product_id']
                # if prod_id not in user_products:
                # if not allow_seen_items and prod_id in user_products:
                #     continue
                if exclude_items and prod_id in exclude_items:
                    continue
                score = row['score']
                product_scores[prod_id] = product_scores.get(prod_id, 0) + score
        
        # Sắp xếp và lấy top N
        top_products = sorted(
            product_scores.items(), 
            key=lambda x: x[1], 
            reverse=True
        )[:num_recommendations]
        
        recommendations = []
        for prod_id, score in top_products:
            product = self.products_df[self.products_df['id'] == prod_id]
            if not product.empty:
                product = product.iloc[0]
                recommendations.append({
                    'product_id': int(prod_id),
                    'similarity_score': float(score),
                    'name': product['name'],
                    'category': product['category_name'],
                    'price': float(product['price']) if product['price'] else None,
                    'recommendation_type': 'COLLABORATIVE'
                })

        print(f"User {user_id} → {len(recommendations)} CF recommendations: {[r['product_id'] for r in recommendations]}")
        return self.normalize_recommendations(recommendations)
    
    # Gợi ý dựa trên Hybrid (Kết hợp Content-based và Collaborative Filtering)
    def get_hybrid_recommendations(
        self,
        user_id: int = None,
        product_id: int = None,
        num_recommendations: int = 10,
        exclude_items: set = None
    ) -> List[Dict]:
        """Kết hợp Content-based và Collaborative Filtering"""
        
        # Đảm bảo cấu hình mới nhất được tải
        self.load_config()
        
        content_recs = []
        collab_recs = []
        
        print(f"Hybrid weights: content={self.content_weight}, collab={self.collaborative_weight}, min_interaction={self.min_interaction_count}")
        
        print("Lấy gợi ý hybrid cho user_id:", user_id, "và product_id:", product_id)
        
        # Lấy gợi ý từ content-based
        if product_id:
            content_recs = self.get_content_based_recommendations(
                product_id, 
                num_recommendations * 2
            )
            print(f"\nTop {len(content_recs)} Content-Based Recs for product {product_id}:")
            for rec in content_recs[:5]:
                print(f"   - {rec['product_id']} | {rec['name']} | score={rec['similarity_score']:.4f}")
        
        # Lấy gợi ý từ collaborative filtering
        if user_id:
            user_interactions = self.interactions_df[self.interactions_df['user_id'] == user_id]
            interaction_count = len(user_interactions)

            # Chỉ dùng CF nếu người dùng có đủ tương tác
            if interaction_count >= self.min_interaction_count:
                print(f"User {user_id} có {interaction_count} tương tác → dùng Collaborative Filtering")
                collab_recs = self.get_collaborative_recommendations(
                    user_id,
                    num_recommendations * 2,
                    exclude_items=exclude_items
                )
                print(f"\nTop {len(collab_recs)} Collaborative Recs for user {user_id}:")
                for rec in collab_recs[:5]:
                    print(f"   - {rec['product_id']} | {rec['name']} | score={rec['similarity_score']:.4f}")
            else:
                print(f"User {user_id} chỉ có {interaction_count} tương tác (< {self.min_interaction_count}) → bỏ qua CF")
                collab_recs = []
                
        # Chuẩn hoá cả hai nguồn điểm trước khi trộn
        content_recs = self.normalize_recommendations(content_recs)
        collab_recs = self.normalize_recommendations(collab_recs)
        
        # Kết hợp kết quả (60% collaborative, 40% content)
        all_recommendations = {}
        
        for rec in collab_recs:
            pid = rec['product_id']
            all_recommendations[pid] = {
                **rec,
                'similarity_score': rec['similarity_score'] * self.collaborative_weight,
                'recommendation_type': 'COLLABORATIVE'
            }
        
        for rec in content_recs:
            pid = rec['product_id']
            if pid in all_recommendations:
                all_recommendations[pid]['similarity_score'] += rec['similarity_score'] * self.content_weight
                all_recommendations[pid]['recommendation_type'] = 'HYBRID'
            else:
                all_recommendations[pid] = {
                    **rec,
                    'similarity_score': rec['similarity_score'] * self.content_weight
                }
        
        # Sắp xếp và trả về top N
        final_recommendations = sorted(
            all_recommendations.values(),
            key=lambda x: x['similarity_score'],
            reverse=True
        )[:num_recommendations]
        print("\nTop Hybrid Results (combined & sorted):")
        for rec in final_recommendations:
            print(f"   - {rec['product_id']} | {rec['name']} | {rec['recommendation_type']} | final_score={rec['similarity_score']:.4f}")

        print("=" * 80 + "\n")
        return final_recommendations
       
recommendation_engine = HybridRecommendationEngine()
