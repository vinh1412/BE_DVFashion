# --- Python 3.10 (Prophet chạy ổn nhất) ---
FROM python:3.10-slim

# --- Install minimal system dependencies ---
RUN apt-get update && apt-get install -y \
    build-essential \
    g++ \
    python3-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# --- Install Python dependencies ---
RUN pip install --upgrade pip

# Không cần pystan / cmdstan
RUN pip install prophet==1.1.5

# Các thư viện còn lại
COPY requirements.txt .
RUN pip install -r requirements.txt

# Copy project files
COPY . .

# Create model folder if missing
RUN mkdir -p /app/model

EXPOSE 8001

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]