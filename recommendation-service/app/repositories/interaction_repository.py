import pandas as pd
from app.db.database import get_db_connection

def fetch_user_interactions():
    """Get all user-product interactions from the database"""
    query = """
    SELECT 
        user_id,
        product_id,
        interaction_type,
        rating,
        interaction_count,
        created_at
    FROM user_product_interactions
    ORDER BY created_at DESC
    """
    conn = get_db_connection()
    df = pd.read_sql(query, conn)
    conn.close()
    return df