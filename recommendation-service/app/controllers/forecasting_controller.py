from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.services.forecasting_service import load_model_and_forecast, train_and_save_model

router = APIRouter()

class ForecastRequest(BaseModel):
    days: int = 30

@router.post("/forecast/revenue")
async def get_revenue_forecast(request: ForecastRequest):
    """
    Dự báo doanh thu cho số ngày tới.
    """
    try:
        forecast_results = load_model_and_forecast(request.days)
        return {"forecast": forecast_results}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/forecast/retrain")
async def retrain_forecast_model():
    """
    Endpoint để yêu cầu huấn luyện lại mô hình (thủ công hoặc qua cron).
    """
    try:
        train_and_save_model()
        return {"message": "Model retraining initiated successfully."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))