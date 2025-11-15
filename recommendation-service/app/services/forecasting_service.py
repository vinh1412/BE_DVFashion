import pandas as pd
from prophet import Prophet
import requests # Để gọi API Java
import json
import numpy as np

# Đường dẫn lưu mô hình đã huấn luyện
MODEL_FILE_PATH = "app/models/revenue_forecast_model.json"

# URL nội bộ để lấy data từ Java backend
JAVA_API_URL = "http://localhost:8080/api/v1/statistics/internal/revenue-timeseries"
# (Thay localhost bằng tên service nếu dùng Docker Compose)

def _fetch_revenue_data():
    """Gọi API Java để lấy dữ liệu doanh thu lịch sử."""
    try:
        # Cần cơ chế xác thực nếu endpoint Java có @PreAuthorize
        # Tạm thời bỏ qua nếu 2 service tin tưởng nhau trong mạng nội bộ
        response = requests.get(JAVA_API_URL)
        response.raise_for_status()
        api_response = response.json()

        data = api_response.get('data', [])
        if not data:
            return pd.DataFrame(columns=['ds', 'y'])

        df = pd.DataFrame(data)
        df = df.rename(columns={"period": "ds", "revenue": "y"})
        df['ds'] = pd.to_datetime(df['ds'])
        return df
    except Exception as e:
        print(f"Error fetching revenue data from Java API: {e}")
        return pd.DataFrame(columns=['ds', 'y'])

def train_and_save_model():
    """
    Huấn luyện mô hình Prophet với dữ liệu mới nhất và lưu lại.
    """
    print("Training revenue forecasting model...")
    df = _fetch_revenue_data()

    df['y'] = df['y'].clip(lower=0)
    df['y'] = np.log1p(df['y'])



    if len(df) < 10: # Cần đủ dữ liệu để train
        print("Not enough data to train model.")
        return

    # Khởi tạo và train Prophet
    model = Prophet(
        daily_seasonality=True,
        weekly_seasonality=True,
        yearly_seasonality=True,
        changepoint_prior_scale=0.05
    )
    # Thêm các ngày lễ (nếu cần, ví dụ: Tết)
    # model.add_country_holidays(country_name='VN')

    model.fit(df)

    # Lưu mô hình (Prophet hỗ trợ lưu/tải bằng JSON)
    from prophet.serialize import model_to_json
    with open(MODEL_FILE_PATH, 'w') as fout:
        json.dump(model_to_json(model), fout)
    print(f"Model trained and saved to {MODEL_FILE_PATH}")

def load_model_and_forecast(days_to_predict: int = 30):
    """
    Tải mô hình đã lưu và dự báo 'days_to_predict' ngày tiếp theo.
    """
    try:
        from prophet.serialize import model_from_json
        with open(MODEL_FILE_PATH, 'r') as fin:
            model = model_from_json(json.load(fin))
    except FileNotFoundError:
        print("Model file not found. Retraining...")
        train_and_save_model()
        with open(MODEL_FILE_PATH, 'r') as fin:
            model = model_from_json(json.load(fin))

    # Tạo dataframe cho tương lai
    future_df = model.make_future_dataframe(periods=days_to_predict, freq='D')

    # Dự báo
    forecast = model.predict(future_df)

    with np.errstate(over='ignore'): # Tắt cảnh báo overflow
        forecast['yhat'] = np.expm1(forecast['yhat'])
        forecast['yhat_lower'] = np.expm1(forecast['yhat_lower'])
        forecast['yhat_upper'] = np.expm1(forecast['yhat_upper'])

    forecast = forecast.replace([np.inf, -np.inf, np.nan], None)

    def clip_at_zero(x):
        if x is None:
            return None
        return max(0, x) # Đảm bảo số >= 0

    forecast['yhat'] = forecast['yhat'].apply(clip_at_zero)
    forecast['yhat_lower'] = forecast['yhat_lower'].apply(clip_at_zero)
    forecast['yhat_upper'] = forecast['yhat_upper'].apply(clip_at_zero)

    # Chỉ lấy dữ liệu dự báo (bỏ qua lịch sử)
    forecast_data = forecast[['ds', 'yhat', 'yhat_lower', 'yhat_upper']].tail(days_to_predict)

    # Chuyển đổi sang định dạng JSON thân thiện
    forecast_data['ds'] = forecast_data['ds'].dt.strftime('%Y-%m-%d')
    return forecast_data.to_dict(orient='records')