import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from fastapi import HTTPException
from typing import List
from app.repositories.product_repository import fetch_products

class RecommendationEngine:
    def __init__(self):
        self.products_df = None
        self.tfidf_matrix = None
        self.vectorizer = None

    # Function to load products data and compute TF-IDF matrix
    def load_products_data(self):
        # Get product data from DB, save to DataFrame products_df.
        self.products_df = fetch_products()
        
        # Combine relevant text fields into a single string for each product
        self.products_df['content_features'] = (
            self.products_df['name'].fillna('') + ' ' +
            self.products_df['description'].fillna('') + ' ' +
            self.products_df['material'].fillna('') + ' ' +
            self.products_df['category_name'].fillna('') + ' ' +
            self.products_df['color'].fillna('')
        )

        # Initialize TF-IDF Vectorizer and compute the TF-IDF matrix
        # max_features=5000: Only consider the top 5000 features
        # ngram_range=(1,2): Consider both unigrams and bigrams
        self.vectorizer = TfidfVectorizer(max_features=5000, ngram_range=(1, 2))
        
        # Fit and transform the content features to create the TF-IDF matrix
        self.tfidf_matrix = self.vectorizer.fit_transform(self.products_df['content_features'])
    
    # Function to get product recommendations based on a given product_id
    def get_recommendations(self, product_id: int, num_recommendations: int = 10) -> List[dict]:
        # Ensure data is loaded
        if self.products_df is None or self.tfidf_matrix is None:
            self.load_products_data()

        # Find the index of the product with the given product_id
        product_indices = self.products_df[self.products_df['id'] == product_id].index
        
        # If product_id not found, raise an error
        if product_indices.empty:
            raise HTTPException(status_code=404, detail=f"Product with id {product_id} not found")

        # Get the index of the product
        product_idx = product_indices[0]

        # Compute cosine similarities between the given product and all other products
        cosine_similarities = cosine_similarity(
            self.tfidf_matrix[product_idx:product_idx+1], self.tfidf_matrix
        ).flatten()
        
        # Get the indices of the top N most similar products (excluding itself)
        similar_indices = cosine_similarities.argsort()[::-1][1:num_recommendations+1]

        # Prepare the list of recommended products with their details
        recommendations = []
        
        # Collect details for each recommended product
        for idx in similar_indices:
            product = self.products_df.iloc[idx]
            recommendations.append({
                'product_id': int(product['id']),
                'similarity_score': float(cosine_similarities[idx]),
                'name': product['name'],
                'category': product['category_name'],
                'price': float(product['price']) if product['price'] else None
            })

        # Return the list of recommendations
        return recommendations


recommendation_engine = RecommendationEngine()
