import pandas as pd
from app.db.database import get_db_connection

def fetch_user_interactions():
    """Load user interactions (mocked CSV data for testing)"""
    import pandas as pd
    df = pd.read_csv("app/data/user_product_interactions_cf_sample.csv")
    print(f"✅ Loaded {len(df)} interactions from sample CSV")
    return df
    
    # """Get all user-product interactions from the database"""
    # query = """
    # SELECT 
    #     user_id,
    #     product_id,
    #     interaction_type,
    #     rating,
    #     interaction_count,
    #     created_at
    # FROM user_product_interactions
    # ORDER BY created_at DESC
    # """
    # conn = get_db_connection()
    # df = pd.read_sql(query, conn)
    # conn.close()
    # return df