# Product Recommendation Service

Đây là một microservice được xây dựng bằng FastAPI để cung cấp các gợi ý sản phẩm dựa trên thuật toán **Content-Based Filtering** và **Collaborative filtering**. Service này phân tích các thuộc tính của sản phẩm (tên, mô tả, danh mục,...) và tương táx người dùng để tìm ra các sản phẩm tương tự.

## Tính năng chính

- **Gợi ý dựa trên nội dung (Content-Based Filtering)**: Tìm các sản phẩm tương tự dựa trên các đặc trưng văn bản.
- **Gợi ý dựa trên ở thích mà người dùng (Collaborative filtering)**: Tìm các sản phẩm tương tự dựa trên xếp hạng và sở thích trước đó của những người dùng khác
- **API Endpoints**: Cung cấp các API để lấy gợi ý và quản lý dữ liệu.
- **Hiệu suất cao**: Sử dụng FastAPI cho hiệu suất cao và tài liệu API tự động.
- **Dễ dàng tích hợp**: Có thể dễ dàng tích hợp vào các hệ thống e-commerce hiện có.

## Cấu trúc thư mục

Dự án được tổ chức theo kiến trúc Layered Architecture để đảm bảo tính module hóa và dễ bảo trì.

```
recommendation-service/
├── app/
│   ├── __init__.py
│   ├── main.py                 # Entrypoint của ứng dụng FastAPI
│   ├── controllers/            # Layer xử lý request/response từ client
│   │   └── recommendation_controller.py
│   ├── services/               # Layer chứa business logic chính
│   │   └── recommendation_service.py
│   ├── repositories/           # Layer chịu trách nhiệm truy vấn dữ liệu từ DB
│   │   ├── product_repository.py
│   │   └── interaction_repository.py
│   ├── models/                 # Chứa các Pydantic schema cho API
│   │   └── schemas.py
│   └── db/                     # Cấu hình và kết nối database
│       └── database.py
├── .env                        # File chứa biến môi trường (local)
├── .gitignore                  # Các file và thư mục được Git bỏ qua
├── requirements.txt            # Danh sách các thư viện Python cần thiết
└── README.md                   # Tài liệu hướng dẫn dự án
```

- **`controllers`**: Nhận request từ client, gọi service tương ứng và trả về response.
- **`services`**: Chứa logic cốt lõi của ứng dụng, ví dụ như thuật toán gợi ý.
- **`repositories`**: Đóng gói logic truy vấn cơ sở dữ liệu, giúp tách biệt business logic khỏi chi tiết về DB.
- **`models`**: Định nghĩa cấu trúc dữ liệu cho request và response của API bằng Pydantic.
- **`db`**: Quản lý việc kết nối đến cơ sở dữ liệu.

## API Endpoints

- **`POST /api/v1/recommendations`**: Lấy danh sách sản phẩm gợi ý.

  - **Request Body**:
    ```json
    {
      "user_id": 26,
      "product_id": 7,
      "num_recommendations": 10,
      "use_collaborative": true
    }
    ```
  - **Response**:
    ```json
    [
      {
          "product_id": 34,
          "similarity_score": 0.12205810296849762,
          "name": "Quần tây nam co giãn",
          "category": "Quần jean nam.",
          "price": 300000.0,
          "recommendation_type": "HYBRID"
      },
      {
          "product_id": 31,
          "similarity_score": 0.11540124563099126,
          "name": "Quần jeans nữ ống rộng",
          "category": "Quần jean nam.",
          "price": 200000.0,
          "recommendation_type": "CONTENT"
      },...
    ]
    ```

- **`POST /api/v1/reload-data`**: Yêu cầu service tải lại dữ liệu từ database và huấn luyện lại mô hình.

## Hướng dẫn cài đặt và chạy service

### Yêu cầu

- Python 3.12+
- PostgreSQL

### Các bước cài đặt

1.  **Clone repository**

    ```bash
    git clone <your-repository-url>
    cd recommendation-service
    ```

2.  **Tạo và kích hoạt môi trường ảo**

    ```bash
    # Tạo môi trường ảo
    python -m venv venv

    # Kích hoạt môi trường ảo
    # Trên Windows
    venv\Scripts\activate
    # Trên macOS/Linux
    source venv/bin/activate
    ```

3.  **Cài đặt các thư viện cần thiết**

    ```bash
    pip install -r requirements.txt
    ```

4.  **Cấu hình biến môi trường**

    Nội dung file `.env`:

    ```env
    APP_HOST=127.0.0.1
    APP_PORT=8001

    DB_HOST=localhost
    DB_NAME=DVFashionDB
    DB_USER=postgres
    DB_PASSWORD=your_db_password
    DB_PORT=5432
    ```

5.  **Chạy service**

    Sử dụng Uvicorn để khởi chạy server. Service sẽ tự động tải lại khi có thay đổi trong code.

    ```bash
    uvicorn app.main:app --reload --host 0.0.0.0 --port 8001
    ```
