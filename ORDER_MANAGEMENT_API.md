# Order Management API Documentation

## Tổng quan

API quản lý đơn hàng cung cấp các tính năng để xem, tìm kiếm và cập nhật trạng thái đơn hàng. Các API này được thiết kế để hỗ trợ các tác vụ quản lý đơn hàng trong hệ thống e-commerce.

## Base URL

```
http://localhost:8083/api/orders
```

## Authentication

Hiện tại API không yêu cầu authentication. Trong tương lai sẽ tích hợp với hệ thống authentication.

## API Endpoints

### 1. Lấy danh sách đơn hàng

**GET** `/api/orders`

Lấy danh sách đơn hàng với phân trang và các bộ lọc.

#### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `userId` | Long | No | - | Lọc theo ID người dùng |
| `status` | String | No | - | Lọc theo trạng thái đơn hàng (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED) |
| `orderCode` | String | No | - | Lọc theo mã đơn hàng (tìm kiếm gần đúng) |
| `fromDate` | DateTime | No | - | Lọc từ ngày tạo |
| `toDate` | DateTime | No | - | Lọc đến ngày tạo |
| `page` | Integer | No | 0 | Số trang (bắt đầu từ 0) |
| `size` | Integer | No | 20 | Số lượng đơn hàng mỗi trang |
| `sortBy` | String | No | createdAt | Trường sắp xếp |
| `sortDirection` | String | No | DESC | Hướng sắp xếp (ASC, DESC) |

#### Response

```json
{
  "content": [
    {
      "id": 1,
      "orderCode": "ORD1234567890ABCDEFGH",
      "userId": 1,
      "status": "PENDING",
      "currency": "VND",
      "subtotal": 200000,
      "discount": 0,
      "shippingFee": 10000,
      "grandTotal": 210000,
      "note": "Test note",
      "createdAt": "2024-01-01T10:00:00",
      "receiverName": "John Doe",
      "receiverPhone": "0123456789",
      "fullAddress": "123 Test Street, Test Ward, Test District, Test City",
      "itemCount": 2
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "hasNext": false,
  "hasPrevious": false
}
```

#### Example Request

```bash
curl -X GET "http://localhost:8083/api/orders?userId=1&status=PENDING&page=0&size=20&sortBy=createdAt&sortDirection=DESC"
```

### 2. Lấy chi tiết đơn hàng

**GET** `/api/orders/{orderId}`

Lấy thông tin chi tiết của một đơn hàng.

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `orderId` | Long | Yes | ID của đơn hàng |

#### Response

```json
{
  "id": 1,
  "orderCode": "ORD1234567890ABCDEFGH",
  "userId": 1,
  "status": "PENDING",
  "currency": "VND",
  "subtotal": 200000,
  "discount": 0,
  "shippingFee": 10000,
  "grandTotal": 210000,
  "note": "Test note",
  "createdAt": "2024-01-01T10:00:00",
  "deliveryAddress": {
    "receiverName": "John Doe",
    "receiverPhone": "0123456789",
    "addressLine1": "123 Test Street",
    "ward": "Test Ward",
    "district": "Test District",
    "city": "Test City",
    "fullAddress": "123 Test Street, Test Ward, Test District, Test City"
  },
  "orderItems": [
    {
      "id": 1,
      "productId": "PROD001",
      "productName": "Test Product",
      "unitPrice": 100000,
      "quantity": 2,
      "lineTotal": 200000
    }
  ]
}
```

#### Example Request

```bash
curl -X GET "http://localhost:8083/api/orders/1"
```

### 3. Cập nhật trạng thái đơn hàng

**PUT** `/api/orders/{orderId}/status`

Cập nhật trạng thái của một đơn hàng.

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `orderId` | Long | Yes | ID của đơn hàng |

#### Request Body

```json
{
  "status": "CONFIRMED",
  "note": "Order confirmed by admin"
}
```

#### Request Body Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `status` | String | Yes | Trạng thái mới (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED) |
| `note` | String | No | Ghi chú về việc thay đổi trạng thái |

#### Response

Trả về thông tin chi tiết đơn hàng đã được cập nhật (giống như GET `/api/orders/{orderId}`).

#### Status Transition Rules

| From Status | To Status | Description |
|-------------|-----------|-------------|
| PENDING | CONFIRMED, CANCELLED | Xác nhận hoặc hủy đơn hàng |
| CONFIRMED | SHIPPED, CANCELLED | Vận chuyển hoặc hủy đơn hàng đã xác nhận |
| SHIPPED | DELIVERED | Giao hàng thành công |
| DELIVERED | REFUNDED | Hoàn tiền cho đơn hàng đã giao |
| CANCELLED | - | Không thể thay đổi trạng thái |
| REFUNDED | - | Không thể thay đổi trạng thái |

#### Example Request

```bash
curl -X PUT "http://localhost:8083/api/orders/1/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED",
    "note": "Order confirmed by admin"
  }'
```

### 4. Tìm kiếm đơn hàng

**GET** `/api/orders/search`

Tìm kiếm đơn hàng theo từ khóa và các bộ lọc.

#### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `keyword` | String | No | - | Từ khóa tìm kiếm (tìm trong mã đơn hàng) |
| `userId` | Long | No | - | Lọc theo ID người dùng |
| `status` | String | No | - | Lọc theo trạng thái đơn hàng |
| `fromDate` | DateTime | No | - | Lọc từ ngày tạo |
| `toDate` | DateTime | No | - | Lọc đến ngày tạo |
| `page` | Integer | No | 0 | Số trang |
| `size` | Integer | No | 20 | Số lượng đơn hàng mỗi trang |
| `sortBy` | String | No | createdAt | Trường sắp xếp |
| `sortDirection` | String | No | DESC | Hướng sắp xếp |

#### Response

Giống như GET `/api/orders` (trả về danh sách đơn hàng với phân trang).

#### Example Request

```bash
curl -X GET "http://localhost:8083/api/orders/search?keyword=ORD123&status=PENDING&page=0&size=20"
```

## Error Responses

### 400 Bad Request

```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Order Validation Error",
  "message": "Status is required"
}
```

### 404 Not Found

```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 404,
  "error": "Order Not Found",
  "message": "Order not found with id: 999"
}
```

### 500 Internal Server Error

```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

## Status Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request thành công |
| 400 | Bad Request - Dữ liệu đầu vào không hợp lệ |
| 404 | Not Found - Không tìm thấy đơn hàng |
| 500 | Internal Server Error - Lỗi server |

## Rate Limiting

Hiện tại API không có rate limiting. Trong tương lai sẽ áp dụng rate limiting để bảo vệ hệ thống.

## Pagination

Tất cả API trả về danh sách đều hỗ trợ phân trang:

- `page`: Số trang (bắt đầu từ 0)
- `size`: Số lượng items mỗi trang (tối đa 100)
- `totalElements`: Tổng số items
- `totalPages`: Tổng số trang
- `first`: Có phải trang đầu tiên không
- `last`: Có phải trang cuối cùng không
- `hasNext`: Có trang tiếp theo không
- `hasPrevious`: Có trang trước đó không

## Sorting

Hỗ trợ sắp xếp theo các trường:

- `createdAt`: Ngày tạo (mặc định)
- `orderCode`: Mã đơn hàng
- `status`: Trạng thái
- `grandTotal`: Tổng tiền

Hướng sắp xếp: `ASC` (tăng dần) hoặc `DESC` (giảm dần, mặc định).

## Filtering

Hỗ trợ các bộ lọc:

- **userId**: Lọc theo ID người dùng
- **status**: Lọc theo trạng thái đơn hàng
- **orderCode**: Tìm kiếm gần đúng trong mã đơn hàng
- **fromDate/toDate**: Lọc theo khoảng thời gian tạo đơn hàng

## Examples

### Lấy tất cả đơn hàng của user 1

```bash
curl -X GET "http://localhost:8083/api/orders?userId=1"
```

### Lấy đơn hàng đang chờ xử lý

```bash
curl -X GET "http://localhost:8083/api/orders?status=PENDING"
```

### Tìm kiếm đơn hàng theo mã

```bash
curl -X GET "http://localhost:8083/api/orders/search?keyword=ORD123"
```

### Xác nhận đơn hàng

```bash
curl -X PUT "http://localhost:8083/api/orders/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED", "note": "Confirmed by admin"}'
```

### Vận chuyển đơn hàng

```bash
curl -X PUT "http://localhost:8083/api/orders/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "SHIPPED", "note": "Shipped via Express"}'
```

### Giao hàng thành công

```bash
curl -X PUT "http://localhost:8083/api/orders/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "DELIVERED", "note": "Delivered successfully"}'
```

### Hủy đơn hàng

```bash
curl -X PUT "http://localhost:8083/api/orders/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "CANCELLED", "note": "Cancelled by customer request"}'
```
