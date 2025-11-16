import pandas as pd
from prophet import Prophet
import requests # Để gọi API Java
import json
import numpy as np
from dotenv import load_dotenv
import os

# Load biến môi trường từ file .env
load_dotenv()

# Đường dẫn lưu mô hình đã huấn luyện
MODEL_FILE_PATH = os.getenv("MODEL_FILE_PATH")

# URL nội bộ để lấy data từ Java backend
JAVA_API_URL = os.getenv("JAVA_API_URL")

def _fetch_revenue_data():
    """Gọi API Java để lấy dữ liệu doanh thu lịch sử."""
    try:
        # Cần cơ chế xác thực nếu endpoint Java có @PreAuthorize
        # Tạm thời bỏ qua nếu 2 service tin tưởng nhau trong mạng nội bộ
        response = requests.get(JAVA_API_URL)
        response.raise_for_status()
        api_response = response.json()

        # Chuyển đổi dữ liệu sang DataFrame
        data = api_response.get('data', [])

        # Xử lý trường hợp không có dữ liệu
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
    Tự động đơn giản hóa mô hình nếu dữ liệu quá ít.
    """
    print("Training revenue forecasting model...")
    df = _fetch_revenue_data()

    print(df.head(20))
    print(df.tail(20))
    print("Total days:", len(df))
    print("From:", df['ds'].min(), "→", df['ds'].max())

    if len(df) < 10: # Cần ít nhất 10 ngày dữ liệu
        print("Not enough data to train model. Need at least 10 data points.")
        return

    # 1. [FIX NAN] Kẹp (clip) các giá trị âm (do hoàn tiền, v.v.)
    df['y'] = df['y'].clip(lower=0)

    # 2. [FIX ÂM/INF] Biến đổi log(y+1)
    df['y'] = np.log1p(df['y'])

    # 3. [FIX BẤT ỔN] ĐƠN GIẢN HÓA MÔ HÌNH
    # Kiểm tra xem có đủ 1 năm dữ liệu không
    has_enough_data_for_yearly = (df['ds'].max() - df['ds'].min()).days >= 365

    # Kiểm tra xem có đủ 3 tuần dữ liệu không (an toàn hơn)
    has_enough_data_for_weekly = (df['ds'].max() - df['ds'].min()).days >= 21

    print(f"Data check: Has yearly data? {has_enough_data_for_yearly}. Has weekly data? {has_enough_data_for_weekly}")

    model = Prophet(
        growth='linear', # Giữ nguyên 'linear', không dùng 'logistic'

        # Tắt các chu kỳ nếu không đủ data
        yearly_seasonality=has_enough_data_for_yearly,
        weekly_seasonality=has_enough_data_for_weekly,
        daily_seasonality=False, # Hầu như không bao giờ cần cho doanh thu hàng ngày

        # Giảm độ nhạy của trend để tránh "lao vọt"
        changepoint_prior_scale=0.01
    )

    model.fit(df)

    # 4. Lưu mô hình
    from prophet.serialize import model_to_json
    with open(MODEL_FILE_PATH, 'w') as fout:
        json.dump(model_to_json(model), fout)
    print(f"Model trained (Simplified: yearly={has_enough_data_for_yearly}) and saved to {MODEL_FILE_PATH}")

def clip_at_zero(x):
    """Đảm bảo giá trị không âm, giữ nguyên None."""
    if x is None:
        return None # Giữ nguyên giá trị null
    return max(0, x) # Đảm bảo các số khác >= 0

def load_model_and_forecast(days_to_predict: int = 30):
    """
    Tải mô hình đã lưu và dự báo 'days_to_predict' ngày tiếp theo.
    (Phiên bản này đầy đủ các bước sửa lỗi)
    """
    try:
        # Tải mô hình từ file
        from prophet.serialize import model_from_json
        with open(MODEL_FILE_PATH, 'r') as fin:
            # Tải mô hình
            model = model_from_json(json.load(fin))
    # Xử lý nếu file mô hình không tồn tại hoặc bị lỗi
    except FileNotFoundError:
        print("Model file not found. Retraining...")
        train_and_save_model()
        # Tải lại mô hình sau khi train
        try:
            with open(MODEL_FILE_PATH, 'r') as fin:
                model = model_from_json(json.load(fin))
        except:
            print("Model file invalid or corrupted. Retraining...")
            train_and_save_model()
            with open(MODEL_FILE_PATH, 'r') as fin:
                model = model_from_json(json.load(fin))

    # 1. Tạo dataframe cho dự báo tương lai
    future_df = model.make_future_dataframe(periods=days_to_predict, freq='D')

    # 2. Dự báo (kết quả vẫn đang ở thang đo LOG)
    forecast = model.predict(future_df)

    # 3. [FIX] Biến đổi ngược
    with np.errstate(over='ignore'):
        forecast['yhat'] = np.expm1(forecast['yhat'])
        forecast['yhat_lower'] = np.expm1(forecast['yhat_lower'])
        forecast['yhat_upper'] = np.expm1(forecast['yhat_upper'])

    # 4. [FIX] Xử lý 'inf' (vô cực) và 'nan' (lỗi tính toán)
    forecast = forecast.replace([np.inf, -np.inf, np.nan], None)

    # 5. [FIX] Đảm bảo không có giá trị âm
    forecast['yhat'] = forecast['yhat'].apply(clip_at_zero)
    forecast['yhat_lower'] = forecast['yhat_lower'].apply(clip_at_zero)
    forecast['yhat_upper'] = forecast['yhat_upper'].apply(clip_at_zero)

    # 6. Chỉ lấy dữ liệu dự báo
    forecast_data = forecast[['ds', 'yhat', 'yhat_lower', 'yhat_upper']].tail(days_to_predict)

    # 7. Chuyển đổi sang định dạng JSON
    forecast_data['ds'] = forecast_data['ds'].dt.strftime('%Y-%m-%d')
    return forecast_data.to_dict(orient='records')