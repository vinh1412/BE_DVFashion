import pandas as pd
from app.db.database import get_db_connection

def fetch_recommendation_config():
    """Read recommendation system configuration from the database"""
    query = "SELECT config_key, config_value FROM recommendation_configs"
    conn = get_db_connection()
    df = pd.read_sql(query, conn)
    conn.close()

    config = {}
    for _, row in df.iterrows():
        key = row['config_key']
        try:
            value = float(row['config_value'])
        except ValueError:
            value = row['config_value']
        config[key] = value
    return config
