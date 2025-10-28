from fastapi import APIRouter, HTTPException
from typing import List
from app.models.schemas import (
    ProductRecommendation, 
    HybridRecommendationRequest,
)
from app.services.recommendation_service import recommendation_engine
from app.services.model_evaluation_service import evaluate_model, evaluate_model_split
from app.db.database import get_db_connection
import pandas as pd

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
    
@router.post("/evaluate")
async def evaluate_current_model(payload: dict):
    """
    Đánh giá mô hình hiện tại và lưu version mới.
    Body:
    {
      "model_name": "hybrid_v3"
    }
    """
    model_name = payload.get("model_name", "hybrid_unknown")
    # result = evaluate_model(model_name=model_name)
    result = evaluate_model_split(model_name=model_name)
    if not result:
        return {"message": "No evaluation data available"}
    return {"message": "Model evaluated successfully", "metrics": result}

@router.get("/models")
async def get_all_model_versions():
    """Lấy danh sách tất cả các phiên bản mô hình"""
    conn = get_db_connection()
    df = pd.read_sql("SELECT * FROM recommendation_model_versions ORDER BY created_at DESC", conn)
    conn.close()
    return df.to_dict(orient="records")

@router.post("/evaluate-split")
async def evaluate_model_split_api(payload: dict):
    """
    Đánh giá mô hình bằng cách chia 80% train / 20% test cho mỗi user
    Body:
    {
        "model_name": "hybrid_v_test",
        "k": 10
    }
    """
    model_name = payload.get("model_name", "hybrid_unknown")
    k = int(payload.get("k", 10))

    result = evaluate_model_split(model_name=model_name, k=k)
    if not result:
        raise HTTPException(status_code=400, detail="Not enough data for evaluation")

    return {
        "message": f"Evaluation for model {model_name} complete",
        "metrics": result
    }