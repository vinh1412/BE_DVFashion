# --- BASE: Python 3.10 (Prophet only works <= 3.10) ---
FROM python:3.10-slim

# --- System packages needed for Prophet & PyStan ---
RUN apt-get update && apt-get install -y \
    build-essential \
    python3-dev \
    g++ \
    libssl-dev \
    libatlas-base-dev \
    && rm -rf /var/lib/apt/lists/*

# --- Set working folder ---
WORKDIR /app

# --- Install Python dependencies ---
# Upgrade pip first
RUN pip install --upgrade pip

# Prophet dependencies
RUN pip install pystan==2.19.1.1 prophet==1.2.1

# Other dependencies
RUN pip install \
    numpy==1.26.4 \
    pandas==2.0.3 \
    scikit-learn==1.3.2 \
    fastapi==0.104.1 \
    uvicorn[standard]==0.24.0 \
    psycopg2-binary==2.9.9 \
    sqlalchemy==2.0.23 \
    python-dotenv==1.0.0 \
    google-generativeai>=0.8.3

# --- Copy project files into container ---
COPY . .

# --- Expose port ---
EXPOSE 8001

# --- Start the API ---
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]