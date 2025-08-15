# 👗 DVFashion Shop - Hệ thống quản lý cửa hàng quần áo thông minh

## 📖 Giới thiệu

DVFashion Shop là một hệ thống quản lý cửa hàng quần áo trực tuyến tích hợp các tính năng thông minh như **gợi ý sản phẩm** và **dự báo doanh thu**. Dự án được phát triển nhằm cung cấp trải nghiệm mua sắm tối ưu cho khách hàng và công cụ quản lý hiệu quả cho chủ shop.

## 🎯 Mục tiêu dự án

- **Cải thiện trải nghiệm khách hàng**: Giao diện thân thiện, tìm kiếm thông minh, gợi ý sản phẩm cá nhân hóa
- **Tối ưu hóa quản lý**: Dự báo doanh thu, quản lý kho hàng, thống kê chi tiết
- **Thanh toán linh hoạt**: Tích hợp PayPal cho thanh toán trực tuyến
- **Đa ngôn ngữ**: Hỗ trợ chuyển đổi Anh - Việt

## ✨ Tính năng chính

### 🛍️ Dành cho Khách hàng

- **Đăng ký/Đăng nhập**: Hỗ trợ đăng ký bằng email hoặc số điện thoại
- **Tìm kiếm & Lọc**: Tìm kiếm thông minh với bộ lọc đa tiêu chí (kích cỡ, màu sắc, giá, giới tính)
- **Gợi ý thông minh**: Đề xuất sản phẩm dựa trên hành vi và sở thích cá nhân
- **Quản lý giỏ hàng**: Thêm, xóa, cập nhật sản phẩm dễ dàng
- **Thanh toán**: Tích hợp PayPal cho thanh toán an toàn
- **Theo dõi đơn hàng**: Kiểm tra trạng thái đơn hàng realtime
- **Đánh giá sản phẩm**: Đánh giá sao và viết nhận xét
- **Chuyển đổi ngôn ngữ**: Hỗ trợ Anh - Việt

### 👨‍💼 Dành cho Quản trị viên

- **Quản lý sản phẩm**: CRUD sản phẩm với đầy đủ thông tin
- **Quản lý kho hàng**: Nhập/xuất kho, kiểm soát tồn kho
- **Dự báo doanh thu**: Sử dụng AI để dự đoán doanh thu tương lai
- **Thống kê & Báo cáo**: Dashboard trực quan, xuất PDF/Excel
- **Quản lý người dùng**: Quản lý tài khoản khách hàng và nhân viên
- **Quản lý khuyến mãi**: Tạo và quản lý các chương trình giảm giá

### 👥 Dành cho Nhân viên

- **Xử lý đơn hàng**: Cập nhật trạng thái, xác nhận đơn hàng
- **Hỗ trợ khách hàng**: Trả lời câu hỏi và tư vấn

## 🏗️ Kiến trúc hệ thống

### Frontend

- **Framework**: ReactJS
- **UI/UX**: Giao diện responsive, thân thiện người dùng
- **Đa ngôn ngữ**: i18n cho Anh - Việt

### Backend

- **Framework**: Java Spring Boot
- **Authentication**: JWT Token
- **API**: RESTful API design

### Database

- **DBMS**: PostgreSQL
- **Schema**: Tối ưu cho hiệu suất và mở rộng

### AI & Machine Learning

- **Engine**: Weka
- **Algorithms**:
  - Collaborative Filtering (gợi ý sản phẩm)
  - Content-based Filtering
  - Time Series Forecasting (dự báo doanh thu)

### Cloud Services

- **Image Management**: Cloudinary
- **Payment**: PayPal Integration

## 📊 Sơ đồ Use Case

Hệ thống được thiết kế với 3 actor chính:

- **Khách hàng**: Mua sắm, đánh giá, theo dõi đơn hàng
- **Quản trị viên**: Quản lý toàn bộ hệ thống
- **Nhân viên**: Xử lý đơn hàng, hỗ trợ khách hàng
- **PayPal System**: Xử lý thanh toán

<img width="2019" height="1879" alt="usecase" src="https://github.com/user-attachments/assets/ac62f08a-5cdd-421e-8899-a7135c32f987" />

## 🚀 Cài đặt và Chạy dự án

### Yêu cầu hệ thống

- Java 17
- Node.js 22+
- PostgreSQL 17

## 📈 Tính năng AI

### 1. Hệ thống Gợi ý Sản phẩm

- **Collaborative Filtering**: Dựa trên hành vi người dùng tương tự
- **Content-based**: Dựa trên đặc điểm sản phẩm
- **Hybrid Approach**: Kết hợp cả hai phương pháp

### 2. Dự báo Doanh thu

- **Input**: Dữ liệu lịch sử bán hàng, mùa vụ, khuyến mãi
- **Output**: Dự báo theo tuần/tháng/quý
- **Accuracy**: Liên tục cải thiện qua feedback

## 🔒 Bảo mật

- **Authentication**: JWT với refresh token
- **Authorization**: Role-based access control
- **Data Protection**: Mã hóa thông tin nhạy cảm
- **API Security**: Rate limiting, input validation

## 📱 Responsive Design

- **Mobile First**: Tối ưu cho thiết bị di động
- **Cross Browser**: Tương thích đa trình duyệt
- **Performance**: Tối ưu tốc độ tải trang

## 🤝 Đội ngũ phát triển

- **Developer 1**: Trần Hiển Vinh
- **Developer 2**: Nguyễn Tấn Thái Dương

## 📄 Giấy phép

Dự án này được phát triển cho mục đích học tập và nghiên cứu.

## 🐛 Báo cáo lỗi

Nếu bạn phát hiện lỗi, vui lòng tạo issue trong repository hoặc liên hệ trực tiếp với đội ngũ phát triển.

## 🔄 Kế hoạch phát triển

### Version 1.1 (Current)

- [ ] Gợi ý sản phẩm AI
- [ ] Dự báo doanh thu
- [ ] Thanh toán PayPal
- [ ] Đa ngôn ngữ
- [ ] Quản lý kho hàng

---

⭐ **Nếu dự án hữu ích, đừng quên cho chúng tôi một star!** ⭐
