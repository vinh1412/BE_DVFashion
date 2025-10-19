from pydantic import BaseModel
from typing import Optional

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
