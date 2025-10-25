from fastapi import APIRouter, HTTPException
from typing import List
from app.models.schemas import (
    ProductRecommendation, 
    HybridRecommendationRequest,
)
from app.services.recommendation_service import recommendation_engine

router = APIRouter()

# Load data on startup
@router.on_event("startup")
async def startup_event():
    print("🔄 Loading product data and interactions...")
    recommendation_engine.load_data()
    print("✅ Data loaded successfully.")

# Endpoint to reload configuration
@router.post("/reload-config")
async def reload_config():
    recommendation_engine.load_config()
    return {"message": "Configuration reloaded successfully"}

# Endpoint to reload data    
@router.post("/reload-data")
async def reload_data():
    try:
        recommendation_engine.load_data()
        return {"message": "Data reloaded successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Endpoint for getting recommendations    
@router.post("/recommendations", response_model=List[ProductRecommendation])
async def get_recommendations(request: HybridRecommendationRequest):
    """
    Product suggestion:
    - If user_id has value → use hybrid (CF + content)
    - If user_id null → use content-based only
    """
    try:
        # If there is no user_id => only use content-based
        if not request.user_id:
            recommendations = recommendation_engine.get_content_based_recommendations(
                product_id=request.product_id,
                num_recommendations=request.num_recommendations
            )
        else:
            recommendations = recommendation_engine.get_hybrid_recommendations(
                user_id=request.user_id,
                product_id=request.product_id,
                num_recommendations=request.num_recommendations
            )

        return [ProductRecommendation(**rec) for rec in recommendations]

    except HTTPException as http_ex:
        raise http_ex
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))    

# Endpoint for hybrid recommendations
@router.post("/recommendations/hybrid", response_model=List[ProductRecommendation])
async def get_hybrid_recommendations(request: HybridRecommendationRequest):
    """API gợi ý sản phẩm hybrid"""
    try:
        recommendations = recommendation_engine.get_hybrid_recommendations(
            user_id=request.user_id,
            product_id=request.product_id,
            num_recommendations=request.num_recommendations
        )
        return [ProductRecommendation(**rec) for rec in recommendations]
    except HTTPException as http_ex:
        raise http_ex
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Endpoint for content-based recommendations
@router.post("/recommendations/content", response_model=List[ProductRecommendation])
async def get_content_based_recommendations(request: HybridRecommendationRequest):
    """
    Content-Based Suggestion API:
    - Based on product content (name, description, category, color,...)
    - No user_id required, just product_id required
    """
    try:
        if not request.product_id:
            raise HTTPException(status_code=400, detail="product_id is required for content-based recommendation")

        recommendations = recommendation_engine.get_content_based_recommendations(
            product_id=request.product_id,
            num_recommendations=request.num_recommendations
        )

        return [ProductRecommendation(**rec) for rec in recommendations]

    except HTTPException as http_ex:
        raise http_ex
    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

# Endpoint for collaborative filtering recommendations
@router.post("/recommendations/collaborative", response_model=List[ProductRecommendation])
async def get_collaborative_recommendations(request: HybridRecommendationRequest):
    """
    Collaborative Filtering Suggestion API:
    - Based on similar user behavior
    - Requires user_id (user must have previous interaction)
    """
    try:
        if not request.user_id:
            raise HTTPException(status_code=400, detail="user_id is required for collaborative recommendation")

        recommendations = recommendation_engine.get_collaborative_recommendations(
            user_id=request.user_id,
            num_recommendations=request.num_recommendations
        )

        return [ProductRecommendation(**rec) for rec in recommendations]

    except HTTPException as http_ex:
        raise http_ex
    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))