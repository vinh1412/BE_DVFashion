from fastapi import APIRouter, HTTPException
from typing import List
from app.models.schemas import ProductRecommendation, RecommendationRequest
from app.services.recommendation_service import recommendation_engine

# Define the API router
router = APIRouter()

# Load data on startup
@router.on_event("startup")
async def startup_event():
    recommendation_engine.load_products_data()

# Endpoint to get product recommendations
@router.post("/recommendations", response_model=List[ProductRecommendation])
async def get_product_recommendations(request: RecommendationRequest):
    try:
        recommendations = recommendation_engine.get_recommendations(
            product_id=request.product_id,
            num_recommendations=request.num_recommendations
        )
        return [ProductRecommendation(**rec) for rec in recommendations]
    except HTTPException as http_ex:
        raise http_ex
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Endpoint to reload data manually
@router.post("/reload-data")
async def reload_data():
    try:
        recommendation_engine.load_products_data()
        return {"message": "Data reloaded successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
