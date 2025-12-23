# 👗 DVFashion – Developing An E-Commerce Website For A Fashion Store With Recommendation Models And Revenue Forecasting Using Machine Learning

An AI-powered fashion e-commerce system that integrates personalized product recommendations and revenue forecasting, designed with a **clear separation between core business logic and AI services** for modern online retail. 🚀


## 1. Project Title & Short Description

**DVFashion – Developing An E-Commerce Website For A Fashion Store With Recommendation Models And Revenue Forecasting Using Machine Learning** is a two-backend project consisting of:

- A **Java Spring Boot core system** that handles all e-commerce business logic.
- A **Python-based AI service** dedicated to product recommendation and revenue forecasting.

The frontend application communicates **only with the Java backend**, while the Java backend internally calls the AI service when intelligent features are required. This architecture is suitable for academic defense, capstone projects, and professional portfolios.


## 2. Introduction & Problem Statement

Many online fashion platforms struggle to simultaneously deliver personalized customer experiences and data-driven business insights. In addition, tightly coupling AI logic with core business systems often leads to poor maintainability and limited scalability.

DVFashion addresses these challenges by:

- Centralizing all business workflows in a stable Java backend.
- Decoupling AI-related computation into a dedicated Python service.
- Enabling personalization and forecasting without exposing AI complexity to the frontend.

This approach improves system maintainability, extensibility, and academic clarity.


## 3. Objectives of the Project

- Provide a complete and secure fashion e-commerce system for customers and administrators.
- Enhance product discovery through AI-based personalized recommendations.
- Support store owners with revenue forecasting for better inventory and business planning.
- Apply a clean, practical architecture suitable for both academic and real-world scenarios.


## 4. System Overview

DVFashion consists of three main components:

- **Frontend (ReactJS)**  
  User interface for customers and administrators.  
  Communicates exclusively with the Java backend via REST APIs.

- **Core Backend (Java Spring Boot)**  
  Handles all business logic including authentication, product management, orders, payments, promotions, and reporting.  
  Acts as a client to the AI service when recommendation or forecasting data is required.

- **AI Service (Python)**  
  Provides REST APIs for product recommendation and revenue forecasting.  
  Does not interact directly with the frontend.

## 5. Diagram

### Usecase Diagram
![Usecase_KLTN_v12](https://github.com/user-attachments/assets/bb62cfa9-26e6-4fd8-b8b9-bbc28b252cd5)

## 6. Key Features

### Customer Features

- 🔍 Smart product search with multi-criteria filters (size, color, price, gender).
- 🛒 Shopping cart and checkout flow with PayPal integration.
- 🤖 Personalized product recommendations on home and product detail pages.
- 📦 Order tracking with status updates.
- 🌐 Bilingual user interface (English / Vietnamese) with responsive design.

### Admin / Manager Features

- 📦 Product, category, variant, and inventory management.
- 🧾 Order management and lifecycle control (create, approve, ship, refund).
- 🎯 Promotion and voucher management.
- 🧑‍💼 User, role, and permission management (RBAC).
- 📊 Revenue analytics and forecasting reports.

### AI / Analytics Features

- Hybrid recommendation combining collaborative filtering and content-based methods.
- Revenue forecasting using Prophet with configurable seasonality.
- Decision support for inventory and marketing planning.


## 7. System Architecture

### High-level Architecture

- Frontend → **Java Backend (single API entry point)**
- Java Backend → **Python AI Service (internal REST calls)**
- Python AI Service → returns prediction/recommendation results to Java
- Java processes and delivers final responses to the frontend

⚠️ The frontend **never communicates directly** with the Python service.

### Component Description

| Component         | Responsibility                      |
| ----------------- | ----------------------------------- |
| Frontend (React)  | User interface                      |
| Java Backend      | Core business logic, APIs, security |
| Python AI Service | Recommendation & forecasting        |
| PostgreSQL        | Primary relational database         |


## 8. Technology Stack

### Backend (Core System)

- Java 17
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA
- RESTful APIs with Swagger/OpenAPI

### AI / Machine Learning

- Python (FastAPI)
- Collaborative Filtering
- Content-based Recommendation
- Prophet for revenue forecasting

### Frontend

- ReactJS
- Tailwind CSS
- Axios / React Query
- i18n support (English / Vietnamese)

### Database

- PostgreSQL

### DevOps / Deployment

- Docker & Docker Compose
- AWS EC2


## 9. Database Design (Brief Overview)

- **Core entities**: Users, Roles, Products, Categories, Variants, Inventory, Orders, OrderItems, Payments, Promotions, Reviews.
- **Relationships**: User–Order (1:N), Order–OrderItems (1:N), Product–Variants (1:N), Product–Reviews (1:N), Promotion–Products (N:M).
- **Optimization**: Indexing on key fields, Redis caching, soft deletes, and audit fields.


## 10. AI / Machine Learning Models

### Product Recommendation

- Hybrid model combining:
  - Collaborative filtering based on user interaction history.
  - Content-based similarity using product attributes.
- Models are executed in the Python service and consumed by the Java backend.

### Revenue Forecasting

- Time-series forecasting using Prophet.
- Supports daily, weekly, and monthly predictions.
- Considers seasonality and historical sales trends.


## 11. API & Communication

- **Frontend ↔ Java Backend**: REST APIs
- **Java Backend ↔ Python AI Service**: Internal REST APIs
- **Authentication**: JWT-based access control
- **Authorization**: Role-based access control (Customer, Admin)
- Architecture allows future extension with async messaging if needed.

## 12. Deployment & Installation

### Prerequisites

- Java 17+, Node.js 18+, Docker 24+, Docker Compose, Git
- PostgreSQL (local or managed)
- PayPal developer credentials and AWS account for cloud deployment

### Local Setup

```bash
git clone https://github.com/vinh1412/BE_DVFashion.git
cd DVFashion

# Backend (example)
./mvnw clean install

# Frontend
cd frontend
npm install
npm run dev
```

### Docker / Cloud Deployment

```bash
# Build and start services locally
docker compose up -d --build

# Deploy to AWS EC2 (illustrative production file)
docker compose -f docker-compose.prod.yml up -d
```

### Env Backend
- For DVFashion
```bash
# Port Configuration
SERVER_PORT=

# Config Database
DB_URL=
DB_USERNAME=
DB_PASSWORD=

# Config Security
JWT_SIGNED_KEY=
JWT_EXPIRATION=
JWT_REFRESH_EXPIRATION=
JWT_SECRET_KEY=

# Config Google OAuth
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

# Config Url
FRONTEND_URL=
BACKEND_URL=

# Config Cloudinary
CLOUDINARY_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

# Config Gemini
GEMINI_KEY=
GEMINI_MODEL=

# Config Email
GMAIL_USERNAME=
GMAIL_APP_PASSWORD=

# Config URL for forgot password
RESET_PASSWORD_URL=

# Recommendation Service
RECOMMENDATION_SERVICE_URL=

# Config PayPal
PAYPAL_BASE_URL=
PAYPAL_CLIENT_ID=
PAYPAL_CLIENT_SECRET=
PAYPAL_MODE=
PAYMENT_SUCCESS_URL=
PAYMENT_CANCEL_URL=

# Ghn Config
GHN_BASE_URL=
GHN_TOKEN=
GHN_SHOP_ID=
GHN_SHOP_NAME=

# Brevo Config
BREVO_API_KEY=
SENDER_EMAIL=
SENDER_NAME=
BREVO_BASE_URL=

# Application Environment
APP_COOKIE_ENV=
```

- For recommendation-service
```bash
# Config server
APP_HOST=
APP_PORT=

# Config connect DB
DB_HOST=
DB_NAME=
DB_USER=
DB_PASSWORD=
DB_PORT=

# Key Gemini API
GEMINI_API_KEY=

MODEL_FILE_PATH=
JAVA_API_URL=
```

## 13. Screenshots / Demo

### Homepage
<img width="1918" height="872" alt="trang chủ" src="https://github.com/user-attachments/assets/d8bfab89-694c-489d-be2a-fb5b19597e6f" />


### Cart
<img width="1918" height="870" alt="giỏ hàng" src="https://github.com/user-attachments/assets/1cb9b14b-740d-4c75-bd28-ed25cfd7a09b" />

### Admin Dashboard
<img width="1918" height="867" alt="trang chủ admin" src="https://github.com/user-attachments/assets/1bb916e5-e19a-4460-9397-bf9314e79334" />

### Statistics
<img width="1917" height="872" alt="trang thống kê" src="https://github.com/user-attachments/assets/0a281eaf-0781-4c6d-b408-d7b9fb62c1c0" />

### Revenue Forecast
<img width="1918" height="867" alt="dự đoán doanh thu" src="https://github.com/user-attachments/assets/fbf2a432-a26c-4052-a289-c96ae954c05d" />

## 14. Future Improvements

- Add A/B testing for recommendation variants.
- Integrate image-based similarity using CNN embeddings.
- Expand observability with distributed tracing and SLO dashboards.
- Implement multi-tenant catalog management for marketplaces.
- Enhance accessibility (WCAG 2.1) across all user flows.

## 15. Conclusion

DVFashion demonstrates a production-ready, AI-augmented fashion e-commerce platform that balances customer delight with operational intelligence. Its modular architecture, modern stack, and robust AI components provide a solid foundation for further research, commercialization, and portfolio presentation.

## 16. Contributors

- Tran Hien Vinh
- Nguyen Tan Thai Duong

## 17. License

This project is released under the MIT License. See `LICENSE` for details.
