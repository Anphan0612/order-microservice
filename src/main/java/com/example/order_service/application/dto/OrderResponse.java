package com.example.order_service.application.dto;

import com.example.order_service.domain.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private Long id;
    private String orderCode;
    private Long userId;
    private OrderStatus status;
    private String currency;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shippingFee;
    private BigDecimal grandTotal;
    private String note;
    private DeliveryAddressResponse deliveryAddress;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> orderItems;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private String productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal lineTotal;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryAddressResponse {
        private String receiverName;
        private String receiverPhone;
        private String addressLine1;
        private String ward;
        private String district;
        private String city;
        private String fullAddress;
    }
}
