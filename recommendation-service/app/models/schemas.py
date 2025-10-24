from pydantic import BaseModel
from typing import Optional, List
from enum import Enum

# Schema for recommendation request and response
class RecommendationRequest(BaseModel):
    product_id: int
    num_recommendations: int = 5

class ProductRecommendation(BaseModel):
    product_id: int
    similarity_score: float
    name: str
    category: str
    price: Optional[float]
    recommendation_type: str

class HybridRecommendationRequest(BaseModel):
    user_id: Optional[int] = None
    product_id: Optional[int] = None
    num_recommendations: int = 10
    use_collaborative: bool = True