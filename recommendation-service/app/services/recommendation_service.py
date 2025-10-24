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

class HybridRecommendationEngine:
    # Khởi tạo các biến cần thiết
    def __init__(self):
        self.products_df = None # DataFrame sản phẩm
        self.interactions_df = None # DataFrame tương tác người dùng
        self.tfidf_matrix = None # Ma trận TF-IDF cho content-based
        self.vectorizer = None # TF-IDF Vectorizer
        self.user_item_matrix = None # Ma trận user-item cho collaborative filtering
        self.knn_model = None # Mô hình KNN cho collaborative filtering
        
        # Trọng số cho các loại tương tác
        self.interaction_weights = {
            'VIEW': 1.0,
            'ADD_TO_CART': 2.0,
            'PURCHASE': 5.0,
            'REVIEW': 3.0
        }

    # Tải dữ liệu sản phẩm và tương tác
    def load_data(self):
        """Load dữ liệu sản phẩm và tương tác"""
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
        
        n_users = user_item_df.shape[0]
        if n_users < 2:
            # không đủ dữ liệu để CF
            self.user_item_matrix = None
            self.knn_model = None
            return
        
        n_neighbors = min(20, n_users)
        
        # Train KNN model
        self.knn_model = NearestNeighbors(
            metric='cosine',
            algorithm='brute',
            n_neighbors=n_neighbors
        )
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
        product_indices = self.products_df[self.products_df['id'] == product_id].index
        
        print("Gợi ý dựa trên nội dung sản phẩm cho product_id:", product_id)
        
        if product_indices.empty:
            raise HTTPException(
                status_code=404, 
                detail=f"Product {product_id} not found"
            )
        
        product_idx = product_indices[0]
        cosine_similarities = cosine_similarity(
            self.tfidf_matrix[product_idx:product_idx+1], 
            self.tfidf_matrix
        ).flatten()
        
        similar_indices = cosine_similarities.argsort()[::-1][1:num_recommendations+1]
        
        recommendations = []
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
        
        return self.normalize_recommendations(recommendations)

    # Gợi ý dựa trên Collaborative Filtering
    def get_collaborative_recommendations(
        self, 
        user_id: int, 
        num_recommendations: int
    ) -> List[Dict]:
        """Gợi ý dựa trên hành vi người dùng tương tự"""
        if self.user_item_matrix is None:
            return []
        
        try:
            user_idx = self.user_ids.index(user_id)
        except ValueError:
            # Người dùng mới chưa có tương tác
            return []
        
        if self.knn_model is None or self.user_item_matrix is None:
            return []

        n_users = self.user_item_matrix.shape[0]
        k = min(11, n_users)
        if k < 2:
            return [] 
        
        # Tìm người dùng tương tự
        distances, indices = self.knn_model.kneighbors(
            self.user_item_matrix[user_idx],
            n_neighbors=k
        )
        
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
                if prod_id not in user_products:
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
        
        return self.normalize_recommendations(recommendations)
    
    # Gợi ý dựa trên Hybrid (Kết hợp Content-based và Collaborative Filtering)
    def get_hybrid_recommendations(
        self,
        user_id: int = None,
        product_id: int = None,
        num_recommendations: int = 10
    ) -> List[Dict]:
        """Kết hợp Content-based và Collaborative Filtering"""
        
        content_recs = []
        collab_recs = []
        
        print("Lấy gợi ý hybrid cho user_id:", user_id, "và product_id:", product_id)
        # Lấy gợi ý từ content-based
        if product_id:
            content_recs = self.get_content_based_recommendations(
                product_id, 
                num_recommendations * 2
            )
        
        # Lấy gợi ý từ collaborative filtering
        if user_id:
            collab_recs = self.get_collaborative_recommendations(
                user_id, 
                num_recommendations * 2
            )
        # 🔧 Chuẩn hoá cả hai nguồn điểm trước khi trộn
        content_recs = self.normalize_recommendations(content_recs)
        collab_recs = self.normalize_recommendations(collab_recs)
        
        # Kết hợp kết quả (60% collaborative, 40% content)
        all_recommendations = {}
        
        for rec in collab_recs:
            pid = rec['product_id']
            all_recommendations[pid] = {
                **rec,
                'similarity_score': rec['similarity_score'] * 0.6,
                'recommendation_type': 'COLLABORATIVE'
            }
        
        for rec in content_recs:
            pid = rec['product_id']
            if pid in all_recommendations:
                all_recommendations[pid]['similarity_score'] += rec['similarity_score'] * 0.4
                all_recommendations[pid]['recommendation_type'] = 'HYBRID'
            else:
                all_recommendations[pid] = {
                    **rec,
                    'similarity_score': rec['similarity_score'] * 0.4
                }
        
        # Sắp xếp và trả về top N
        final_recommendations = sorted(
            all_recommendations.values(),
            key=lambda x: x['similarity_score'],
            reverse=True
        )[:num_recommendations]
        
        return final_recommendations


recommendation_engine = HybridRecommendationEngine()
