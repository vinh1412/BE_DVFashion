from app.db.database import get_db_connection

def save_model_evaluation(model_name, content_weight, collaborative_weight,
                          precision_at_10, recall_at_10, map_at_10, is_active=False):
    """Lưu thông tin đánh giá mô hình vào DB"""
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        cursor.execute("""
            INSERT INTO recommendation_model_versions
            (model_name, content_weight, collaborative_weight, precision_at_10, recall_at_10, map_at_10, is_active)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
        """, (model_name, content_weight, collaborative_weight,
              precision_at_10, recall_at_10, map_at_10, is_active))
        conn.commit()
    finally:
        cursor.close()
        conn.close()
