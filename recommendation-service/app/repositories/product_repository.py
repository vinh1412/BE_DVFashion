import pandas as pd
from app.db.database import get_db_connection

# Function to fetch products from the database
def fetch_products():
    # """Mock data products for testing"""
    # import pandas as pd
    # df = pd.read_csv("app/data/products_cf_sample.csv")
    # print(f"✅ Loaded {len(df)} products from CSV")
    # return df
  
    """Get all active products with their translations and variants"""
    query = """
    SELECT 
        p.id,
        p.category_id,
        p.price,
        p.sale_price,
        pt.name,
        pt.description,
        pt.material,
        ct.name as category_name,
        pv.color
    FROM products p
    JOIN product_translations pt ON p.id = pt.product_id AND pt.language = 'VI'
    JOIN category_translations ct ON p.category_id = ct.category_id AND ct.language = 'VI'
    JOIN product_variants pv ON p.id = pv.product_id
    WHERE p.status = 'ACTIVE'
      AND pv.status = 'ACTIVE'
    """
    conn = get_db_connection()
    df = pd.read_sql(query, conn)
    conn.close()
    return df