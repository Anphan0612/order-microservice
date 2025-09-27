# Flyway Migration Guide

## Tổng quan

Flyway được sử dụng để quản lý database migration trong Order Service. Tất cả các file migration được lưu trong `src/main/resources/db/migration/`.

## Cấu hình

### Dependencies trong pom.xml
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

### Maven Plugin
```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>10.8.1</version>
    <configuration>
        <url>jdbc:mysql://localhost:3306/orderservice</url>
        <user>root</user>
        <password>1234</password>
        <locations>
            <location>classpath:db/migration</location>
        </locations>
    </configuration>
</plugin>
```

### Application Properties
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

## Migration Files

### Cấu trúc file migration
- Tên file: `V{version}__{description}.sql`
- Ví dụ: `V1__init_order.sql`
- Version phải là số nguyên tăng dần
- Description có thể chứa dấu gạch dưới

### File migration hiện tại
- `V1__init_order.sql`: Tạo các bảng cơ bản (orders, order_items, idempotency_keys, outbox_events)

## Commands

### Kiểm tra trạng thái
```bash
mvn flyway:info
```
Hiển thị:
- Các migration đã chạy
- Các migration chưa chạy
- Trạng thái hiện tại của database

### Chạy migration
```bash
mvn flyway:migrate
```
Chạy tất cả migration chưa được áp dụng.

### Validate migration
```bash
mvn flyway:validate
```
Kiểm tra tính hợp lệ của migration files.

### Tạo baseline
```bash
mvn flyway:baseline
```
Tạo baseline cho database đã có dữ liệu.

### Clean database
```bash
mvn flyway:clean
```
⚠️ **CẢNH BÁO**: Xóa tất cả objects trong database!

### Repair migration
```bash
mvn flyway:repair
```
Sửa chữa metadata table nếu bị lỗi.

## Workflow

### 1. Tạo migration mới
1. Tạo file SQL mới trong `src/main/resources/db/migration/`
2. Đặt tên theo format: `V{next_version}__{description}.sql`
3. Viết SQL migration

### 2. Test migration
```bash
# Kiểm tra syntax
mvn flyway:validate

# Chạy migration
mvn flyway:migrate

# Kiểm tra kết quả
mvn flyway:info
```

### 3. Rollback (nếu cần)
Flyway không hỗ trợ rollback tự động. Cần tạo migration mới để undo changes.

## Best Practices

1. **Luôn backup database** trước khi chạy migration
2. **Test migration** trên database development trước
3. **Không sửa file migration** đã chạy
4. **Sử dụng transaction** cho migration phức tạp
5. **Đặt tên file rõ ràng** và mô tả chức năng

## Troubleshooting

### Lỗi migration đã chạy
```
FlywayException: Validate failed: Migration checksum mismatch
```
**Giải pháp**: Sử dụng `mvn flyway:repair` hoặc tạo migration mới.

### Lỗi kết nối database
```
FlywayException: Unable to connect to database
```
**Giải pháp**: Kiểm tra cấu hình database trong pom.xml và application.properties.

### Lỗi syntax SQL
```
FlywayException: Migration failed
```
**Giải pháp**: Kiểm tra syntax SQL trong file migration.

## Monitoring

### Kiểm tra migration history
```sql
SELECT * FROM flyway_schema_history;
```

### Kiểm tra tables được tạo
```sql
SHOW TABLES;
```

## Integration với Spring Boot

Spring Boot sẽ tự động chạy migration khi:
- `spring.flyway.enabled=true`
- Application startup
- Database connection thành công

Migration sẽ chạy trước khi JPA entities được khởi tạo.
