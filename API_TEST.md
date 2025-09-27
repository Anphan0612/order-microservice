# API Testing Guide - Order Create Feature

## Chuẩn bị

1. **Tạo database**:
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

4. **Kiểm tra service đang chạy**:
   ```bash
   curl http://localhost:8083/api/orders/health
   ```

## Flyway Migration

### Kiểm tra trạng thái migration
```bash
mvn flyway:info
```

### Chạy migration thủ công
```bash
mvn flyway:migrate
```

### Validate migration
```bash
mvn flyway:validate
```

## Test tạo đơn hàng

### Test case 1: Tạo đơn hàng thành công

```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-order-001" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": "PROD001",
        "productName": "Bánh mì thịt nướng",
        "unitPrice": 25000,
        "quantity": 2
      },
      {
        "productId": "PROD002", 
        "productName": "Cà phê đen",
        "unitPrice": 15000,
        "quantity": 1
      }
    ],
    "deliveryAddress": {
      "receiverName": "Nguyễn Văn A",
      "receiverPhone": "0123456789",
      "addressLine1": "123 Đường ABC",
      "ward": "Phường 1",
      "district": "Quận 1",
      "city": "TP. Hồ Chí Minh"
    },
    "discount": 5000,
    "shippingFee": 10000,
    "note": "Giao hàng trước 12h"
  }'
```

**Expected Response** (201 Created):
```json
{
  "id": 1,
  "orderCode": "ORD1234567890ABCDEFGH",
  "userId": 1,
  "status": "PENDING",
  "currency": "VND",
  "subtotal": 65000,
  "discount": 5000,
  "shippingFee": 10000,
  "grandTotal": 70000,
  "note": "Giao hàng trước 12h",
  "deliveryAddress": {
    "receiverName": "Nguyễn Văn A",
    "receiverPhone": "0123456789",
    "addressLine1": "123 Đường ABC",
    "ward": "Phường 1",
    "district": "Quận 1",
    "city": "TP. Hồ Chí Minh",
    "fullAddress": "123 Đường ABC, Phường 1, Quận 1, TP. Hồ Chí Minh"
  },
  "createdAt": "2024-01-01T10:00:00",
  "orderItems": [
    {
      "id": 1,
      "productId": "PROD001",
      "productName": "Bánh mì thịt nướng",
      "unitPrice": 25000,
      "quantity": 2,
      "lineTotal": 50000
    },
    {
      "id": 2,
      "productId": "PROD002",
      "productName": "Cà phê đen", 
      "unitPrice": 15000,
      "quantity": 1,
      "lineTotal": 15000
    }
  ]
}
```

### Test case 2: Test idempotency

Chạy lại cùng request với cùng Idempotency-Key:

```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-order-001" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": "PROD001",
        "productName": "Bánh mì thịt nướng",
        "unitPrice": 25000,
        "quantity": 2
      }
    ],
    "deliveryAddress": {
      "receiverName": "Nguyễn Văn A",
      "receiverPhone": "0123456789",
      "addressLine1": "123 Đường ABC",
      "ward": "Phường 1",
      "district": "Quận 1",
      "city": "TP. Hồ Chí Minh"
    }
  }'
```

**Expected**: Trả về cùng order đã tạo trước đó (idempotent)

### Test case 3: Test validation errors

#### Thiếu Idempotency-Key:
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderItems": [],
    "deliveryAddress": {
      "receiverName": "Test",
      "receiverPhone": "0123456789",
      "addressLine1": "123 Test",
      "ward": "Test",
      "district": "Test", 
      "city": "Test"
    }
  }'
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Order Validation Error",
  "message": "Idempotency key is required",
  "path": "/api/orders"
}
```

#### Thiếu order items:
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-validation" \
  -d '{
    "userId": 1,
    "orderItems": [],
    "deliveryAddress": {
      "receiverName": "Test",
      "receiverPhone": "0123456789",
      "addressLine1": "123 Test",
      "ward": "Test",
      "district": "Test",
      "city": "Test"
    }
  }'
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "path": "/api/orders",
  "details": {
    "orderItems": "Order items cannot be empty"
  }
}
```

## Kiểm tra database

Sau khi tạo đơn hàng thành công, kiểm tra các bảng trong database:

```sql
-- Kiểm tra đơn hàng
SELECT * FROM orders;

-- Kiểm tra chi tiết đơn hàng
SELECT * FROM order_items;

-- Kiểm tra idempotency key
SELECT * FROM idempotency_keys;

-- Kiểm tra outbox events
SELECT * FROM outbox_events;
```

## Monitoring

- **Health check**: `GET http://localhost:8083/actuator/health`
- **Application info**: `GET http://localhost:8083/actuator/info`
- **Logs**: Xem logs trong console để theo dõi quá trình tạo đơn hàng
