# Order Service - Order Management System

Order Service là một microservice được phát triển theo kiến trúc Domain-Driven Design (DDD), cung cấp đầy đủ các tính năng **quản lý đơn hàng** bao gồm tạo, xem, tìm kiếm và cập nhật trạng thái đơn hàng sử dụng Spring Boot và MySQL database.

## Kiến trúc

Service được tổ chức theo kiến trúc DDD với các layer sau:

```
src/main/java/com/example/order_service/
├── application/          # Application layer
│   ├── dto/             # Data Transfer Objects
│   └── usecase/         # Use cases (business logic)
├── domain/              # Domain layer
│   ├── exception/       # Custom exceptions
│   ├── model/           # Domain entities
│   └── repository/      # Repository interfaces
├── infrastructure/      # Infrastructure layer
│   └── service/         # External service clients (placeholder)
└── interfaces/          # Interface layer
    └── rest/            # REST controllers
```

## Tính năng chính

### Quản lý đơn hàng
- **Tạo đơn hàng**: Tạo đơn hàng mới với validation và idempotency
- **Xem danh sách đơn hàng**: Lấy danh sách đơn hàng với phân trang và bộ lọc
- **Xem chi tiết đơn hàng**: Lấy thông tin chi tiết của một đơn hàng
- **Tìm kiếm đơn hàng**: Tìm kiếm đơn hàng theo các tiêu chí khác nhau
- **Cập nhật trạng thái**: Cập nhật trạng thái đơn hàng (confirm, ship, deliver, cancel)

### Tính năng hỗ trợ
- **Validation**: Kiểm tra dữ liệu đầu vào cơ bản
- **Idempotency**: Đảm bảo tính idempotent cho các request tạo đơn hàng
- **Event-driven**: Sử dụng outbox pattern để gửi events
- **Database persistence**: Lưu trữ đơn hàng vào MySQL
- **Pagination**: Hỗ trợ phân trang cho tất cả API danh sách
- **Filtering**: Hỗ trợ lọc theo nhiều tiêu chí
- **Sorting**: Hỗ trợ sắp xếp theo các trường khác nhau

## Công nghệ sử dụng

- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **MySQL 8**
- **Flyway** (database migration)
- **Lombok**
- **H2** (cho testing)

## Cấu hình

### Database
- MySQL 8
- Database name: `orderservice`
- Username: `root`
- Password: `1234`
- Port: `3306`

### Port
- Service port: `8083`

## API Endpoints

### 1. Tạo đơn hàng
```http
POST /api/orders
Content-Type: application/json
Idempotency-Key: <unique-key>

{
  "userId": 1,
  "orderItems": [
    {
      "productId": "PROD001",
      "productName": "Product Name",
      "unitPrice": 100000,
      "quantity": 2
    }
  ],
  "deliveryAddress": {
    "receiverName": "John Doe",
    "receiverPhone": "0123456789",
    "addressLine1": "123 Test Street",
    "ward": "Test Ward",
    "district": "Test District",
    "city": "Test City"
  },
  "discount": 0,
  "shippingFee": 10000,
  "note": "Optional note"
}
```

### 2. Xem danh sách đơn hàng
```http
GET /api/orders?userId=1&status=PENDING&page=0&size=20&sortBy=createdAt&sortDirection=DESC
```

### 3. Xem chi tiết đơn hàng
```http
GET /api/orders/{orderId}
```

### 4. Cập nhật trạng thái đơn hàng
```http
PUT /api/orders/{orderId}/status
Content-Type: application/json

{
  "status": "CONFIRMED",
  "note": "Order confirmed by admin"
}
```

### 5. Tìm kiếm đơn hàng
```http
GET /api/orders/search?keyword=ORD123&status=PENDING&page=0&size=20
```

### 6. Health Check
```http
GET /api/orders/health
```

## API Documentation

Chi tiết đầy đủ về các API endpoints có thể xem tại:
- [API_TEST.md](API_TEST.md) - Hướng dẫn test API tạo đơn hàng
- [ORDER_MANAGEMENT_API.md](ORDER_MANAGEMENT_API.md) - Tài liệu API quản lý đơn hàng

## Chạy ứng dụng

### Yêu cầu
- Java 17+
- Maven 3.6+
- MySQL 8+

### Cách chạy

1. **Cài đặt database**:
   ```sql
   CREATE DATABASE orderservice;
   ```

2. **Chạy migration (tùy chọn)**:
   ```bash
   # Chạy migration bằng Flyway Maven plugin
   mvn flyway:migrate
   
   # Hoặc để Spring Boot tự động chạy migration khi start
   ```

3. **Chạy ứng dụng**:
   ```bash
   mvn spring-boot:run
   ```

4. **Chạy tests**:
   ```bash
   mvn test
   ```

### Flyway Commands

```bash
# Kiểm tra trạng thái migration
mvn flyway:info

# Chạy migration
mvn flyway:migrate

# Tạo baseline (nếu database đã có dữ liệu)
mvn flyway:baseline

# Validate migration
mvn flyway:validate

# Clean database (xóa tất cả objects)
mvn flyway:clean
```

## Tích hợp với các service khác (Tương lai)

Service này được thiết kế để tích hợp với các service sau:

- **User Service**: Xác thực user khi tạo đơn hàng
- **Product Service**: Xác thực sản phẩm trong đơn hàng
- **Payment Service**: Xác thực phương thức thanh toán

Hiện tại, các validation cơ bản được thực hiện trong code. Khi các service khác sẵn sàng, có thể dễ dàng tích hợp thông qua Feign clients.

## Database Schema

### Bảng `orders`
- `id`: Primary key
- `order_code`: Mã đơn hàng (unique)
- `user_id`: ID của user
- `status`: Trạng thái đơn hàng
- `currency`: Loại tiền tệ
- `subtotal`: Tổng tiền hàng
- `discount`: Giảm giá
- `shipping_fee`: Phí vận chuyển
- `grand_total`: Tổng cộng
- `note`: Ghi chú
- `receiver_name`, `receiver_phone`: Thông tin người nhận
- `address_line1`, `ward`, `district`, `city`: Địa chỉ giao hàng
- `created_at`: Thời gian tạo

### Bảng `order_items`
- `id`: Primary key
- `order_id`: Foreign key đến orders
- `product_id`: ID sản phẩm
- `product_name`: Tên sản phẩm
- `unit_price`: Giá đơn vị
- `quantity`: Số lượng
- `line_total`: Thành tiền

### Bảng `idempotency_keys`
- `id`: Primary key
- `user_id`: ID của user
- `idem_key`: Idempotency key
- `request_hash`: Hash của request
- `order_id`: ID đơn hàng đã tạo
- `created_at`: Thời gian tạo

### Bảng `outbox_events`
- `id`: Primary key
- `aggregate_type`: Loại aggregate
- `aggregate_id`: ID của aggregate
- `type`: Loại event
- `payload`: Dữ liệu event (JSON)
- `status`: Trạng thái event
- `created_at`: Thời gian tạo

## Monitoring

Service hỗ trợ Spring Boot Actuator với các endpoints:
- `/actuator/health`: Health check
- `/actuator/info`: Thông tin service

## Logging

Service sử dụng SLF4J với Logback. Cấu hình logging trong `application.properties`:
- Hibernate SQL: DEBUG level
- Spring Security: DEBUG level
- Application logs: INFO level
