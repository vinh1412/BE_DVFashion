import pandas as pd
from app.db.database import get_db_connection

def fetch_products():
    """
    Lấy tất cả sản phẩm đang hoạt động (ACTIVE) kèm bản dịch tiếng Việt,
    màu sắc (color), kích thước (size_name) và danh mục (category_name).
    """
    query = """
    SELECT 
        p.id,
        p.category_id,
        p.price,
        COALESCE(p.sale_price, p.price, 0) AS sale_price,
        pt.name,
        pt.description,
        pt.material,
        ct.name AS category_name,
        pv.id AS variant_id,
        pv.color,
        s.size_name
    FROM products p
    JOIN product_translations pt 
        ON p.id = pt.product_id AND pt.language = 'VI'
    JOIN category_translations ct 
        ON p.category_id = ct.category_id AND ct.language = 'VI'
    JOIN product_variants pv 
        ON p.id = pv.product_id
    JOIN sizes s 
        ON pv.id = s.product_variant_id
    WHERE p.status = 'ACTIVE'
      AND pv.status = 'ACTIVE'
    """
    conn = get_db_connection()
    df = pd.read_sql(query, conn)
    conn.close()

    # Gộp các biến thể (màu sắc, kích thước) vào cùng một dòng sản phẩm
    df = (
    df.groupby("id")
    .agg({
        "name": "first",
        "description": "first",
        "material": "first",
        "category_name": "first",
        "price": "first",
        "sale_price": "first",
        # Gộp tất cả màu sắc thành chuỗi không trùng
        "color": lambda x: ", ".join(sorted(set(x.dropna()))),
        # Gộp tất cả size thành chuỗi không trùng
        "size_name": lambda x: ", ".join(sorted(set(x.dropna())))
    })
    .reset_index()
    )
    
    print("✅ Fetched products from database", df)

    print(f"✅ Loaded {len(df)} products (merged by color + sizes)")
    return df