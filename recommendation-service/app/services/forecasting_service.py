import pandas as pd
import json
import numpy as np
from dotenv import load_dotenv
import os
from datetime import timedelta

# Load biến môi trường từ file .env
load_dotenv()

# Đường dẫn lưu mô hình đã huấn luyện
MODEL_FILE_PATH = os.getenv("MODEL_FILE_PATH")

if not MODEL_FILE_PATH or not os.path.exists(MODEL_FILE_PATH):
    raise FileNotFoundError(f"MODEL_FILE_PATH invalid or file missing: {MODEL_FILE_PATH}")

def clip_at_zero(x):
    """Đảm bảo giá trị không âm, giữ nguyên None."""
    if x is None:
        return None
    return max(0, x)

def load_model_and_forecast(days_to_predict: int = 30):
    """
    Tải mô hình đã lưu và dự báo 'days_to_predict' ngày tiếp theo.
    Chỉ sử dụng model đã train sẵn, không train lại.
    """
    try:
        # Tải mô hình từ file
        from prophet.serialize import model_from_json
        with open(MODEL_FILE_PATH, 'r') as fin:
            model = model_from_json(json.load(fin))
    except FileNotFoundError:
        raise FileNotFoundError(f"Model file not found at {MODEL_FILE_PATH}. Please train the model first.")
    except Exception as e:
        raise Exception(f"Error loading model: {str(e)}")

    # Lấy ngày hiện tại
    today = pd.Timestamp.now().normalize()

    # Tạo các ngày trong tương lai (từ ngày mai)
    future_dates = pd.date_range(
        start=today + timedelta(days=1),
        periods=days_to_predict,
        freq='D'
    )

    future_df = pd.DataFrame({'ds': future_dates})

    # Dự báo (kết quả vẫn đang ở thang đo LOG)
    forecast = model.predict(future_df)

    # Biến đổi ngược từ log
    with np.errstate(over='ignore'):
        forecast['yhat'] = np.expm1(forecast['yhat'])
        forecast['yhat_lower'] = np.expm1(forecast['yhat_lower'])
        forecast['yhat_upper'] = np.expm1(forecast['yhat_upper'])

    # Xử lý 'inf' (vô cực) và 'nan' (lỗi tính toán)
    forecast = forecast.replace([np.inf, -np.inf, np.nan], None)

    # Đảm bảo không có giá trị âm
    forecast['yhat'] = forecast['yhat'].apply(clip_at_zero)
    forecast['yhat_lower'] = forecast['yhat_lower'].apply(clip_at_zero)
    forecast['yhat_upper'] = forecast['yhat_upper'].apply(clip_at_zero)

    # Chỉ lấy dữ liệu dự báo
    forecast_data = forecast[['ds', 'yhat', 'yhat_lower', 'yhat_upper']].tail(days_to_predict)

    # Chuyển đổi sang định dạng JSON
    forecast_data['ds'] = forecast_data['ds'].dt.strftime('%Y-%m-%d')
    return forecast_data.to_dict(orient='records')