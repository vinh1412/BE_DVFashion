import os
import json
from typing import Tuple, List, Dict

import google.generativeai as genai

from app.services.recommendation_service import recommendation_engine

# Cấu hình Gemini
genai.configure(api_key=os.getenv('GEMINI_API_KEY'))

# Khởi tạo model Gemini
model = genai.GenerativeModel("gemini-2.5-flash")

# (tuỳ chọn) knowledge base về shop: chính sách, đổi trả, giao hàng...
SHOP_INFO = """
Bạn là trợ lý AI của shop thời trang DVFashion.
Chính sách cơ bản:
- Thời gian đổi trả: 7 ngày kể từ khi nhận hàng, sản phẩm còn tag, chưa giặt, không hư hỏng.
- Hình thức thanh toán: COD, chuyển khoản bằng Paypal.
- Thời gian giao hàng: 2-5 ngày làm việc tùy khu vực.
"""

# Hàm tiện ích
def _extract_json(raw_text: str) -> dict:
    """
    Gemini đôi khi trả về JSON kèm ```json ...```, hàm này sẽ tách ra và parse.
    """
    text = raw_text.strip()
    if text.startswith("```"):
        # loại bỏ ```json hoặc ``` ở đầu/cuối
        text = text.strip("`")
        if text.lower().startswith("json"):
            text = text[4:].strip()
    return json.loads(text)

# Hàm chính của dịch vụ chat AI
def analyze_intent(user_message: str) -> Tuple[str, str]:
    """
    Gọi Gemini để phân tích intent & query tìm sản phẩm.
    Trả về (intent, query).
    """
    prompt = f"""
    Bạn là bộ phân tích câu hỏi khách hàng cho shop thời trang.

    Nhiệm vụ:
    - Đọc câu của khách.
    - Nếu khách đang muốn tìm/mua sản phẩm, hãy gán intent = "TIM_SAN_PHAM".
    - Nếu khách hỏi về khuyến mãi, mã giảm giá: intent = "HOI_KHUYEN_MAI".
    - Nếu khách hỏi về đổi trả, giao hàng, thanh toán: intent = "HOI_CHINH_SACH".
    - Ngược lại: intent = "KHAC".

    Đồng thời:
    - Nếu intent = "TIM_SAN_PHAM", hãy trích xuất mô tả sản phẩm ngắn gọn làm query, dùng tiếng Việt không dấu cũng được.

    Hãy trả về đúng một JSON với cấu trúc:
    {{
      "intent": "TIM_SAN_PHAM" | "HOI_KHUYEN_MAI" | "HOI_CHINH_SACH" | "KHAC",
      "query": "mô tả sản phẩm hoặc rỗng"
    }}

    Câu của khách: "{user_message}"
    """

    resp = model.generate_content(prompt)
    data = _extract_json(resp.text)
    intent = data.get("intent", "KHAC")
    query = data.get("query", "").strip()
    return intent, query

# Hàm xây dựng câu trả lời dựa trên sản phẩm
def build_product_list_for_llm(products: List[Dict]) -> str:
    """
    Chuyển list sản phẩm thành text gọn cho Gemini.
    """
    lines = []
    for p in products:
        price_txt = f"{p['price']:.0f} VND" if p.get("price") is not None else "Liên hệ"
        lines.append(
            f"- ID: {p['product_id']}, Tên: {p['name']}, "
            f"Danh mục: {p['category']}, Giá: {price_txt}"
        )
    return "\n".join(lines)

# Hàm trả lời với danh sách sản phẩm gợi ý
def answer_with_products(user_message: str, products: List[Dict]) -> str:
    """
    Gọi Gemini để tạo câu trả lời tự nhiên, dựa trên danh sách sản phẩm gợi ý.
    """
    products_text = build_product_list_for_llm(products)
    prompt = f"""
    {SHOP_INFO}

    Khách vừa hỏi: "{user_message}"

    Dưới đây là các sản phẩm phù hợp lấy từ database của shop:
    {products_text}

    Hãy trả lời:
    - Giải thích ngắn gọn vì sao đây là các sản phẩm phù hợp.
    - Gợi ý 2-3 lựa chọn nổi bật.
    - Văn phong thân thiện, xưng "shop" với khách.

    Trả lời bằng tiếng Việt.
    """
    resp = model.generate_content(prompt)
    return resp.text.strip()

# Hàm trả lời các câu hỏi chung
def answer_general_question(user_message: str) -> str:
    """
    Trả lời các câu hỏi không phải tìm sản phẩm (chính sách, khuyến mãi, câu hỏi chung).
    """
    prompt = f"""
    {SHOP_INFO}

    Khách hỏi: "{user_message}"

    Hãy trả lời đúng theo thông tin của shop ở trên, ngắn gọn, dễ hiểu.
    """
    resp = model.generate_content(prompt)
    return resp.text.strip()

# Hàm chính xử lý message từ frontend
async def process_message(user_message: str) -> Dict:
    """
    Hàm chính: nhận message từ frontend, trả về JSON cho chatbot.
    """
    intent, query = analyze_intent(user_message)

    if intent == "TIM_SAN_PHAM":
        # Dùng mô tả query để tìm sản phẩm
        recommendations = recommendation_engine.get_recommendations_by_text(
            query=query or user_message,
            num_recommendations=5
        )
        reply = answer_with_products(user_message, recommendations)
        return {
            "intent": intent,
            "reply": reply,
            "products": recommendations
        }

    else:
        # Các intent khác: để Gemini trả lời dựa trên SHOP_INFO
        reply = answer_general_question(user_message)
        return {
            "intent": intent,
            "reply": reply,
            "products": []
        }
