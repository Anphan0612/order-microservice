package com.example.order_service.application.usecase;

import com.example.order_service.application.dto.CreateOrderRequest;
import com.example.order_service.application.dto.OrderResponse;
import com.example.order_service.domain.exception.OrderValidationException;
import com.example.order_service.domain.model.DeliveryAddress;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderItem;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderUseCase {
    
    private final OrderRepository orderRepository;
    
    @Transactional
    public OrderResponse execute(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());
        
        // Validate request
        validateRequest(request);
        
        // Create order
        Order order = createOrder(request);
        order = orderRepository.save(order);
        
        log.info("Order created successfully: {}", order.getOrderCode());
        return mapToResponse(order);
    }
    
    private void validateRequest(CreateOrderRequest request) {
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new OrderValidationException("Invalid user ID: " + request.getUserId());
        }
        
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new OrderValidationException("Order items cannot be empty");
        }
        
        if (request.getDeliveryAddress() == null) {
            throw new OrderValidationException("Delivery address is required");
        }
        
        // Validate order items
        for (CreateOrderRequest.OrderItemRequest item : request.getOrderItems()) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                throw new OrderValidationException("Product ID cannot be empty");
            }
            if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
                throw new OrderValidationException("Product name cannot be empty");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new OrderValidationException("Unit price must be positive");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new OrderValidationException("Quantity must be positive");
            }
        }
        
        log.debug("Request validation passed for user: {}", request.getUserId());
    }
    
    private Order createOrder(CreateOrderRequest request) {
        String orderCode = generateOrderCode();
        
        Order order = Order.builder()
                .orderCode(orderCode)
                .userId(request.getUserId())
                .status(OrderStatus.PENDING)
                .currency("VND")
                .discount(request.getDiscount() != null ? request.getDiscount() : java.math.BigDecimal.ZERO)
                .shippingFee(request.getShippingFee() != null ? request.getShippingFee() : java.math.BigDecimal.ZERO)
                .note(request.getNote())
                .deliveryAddress(mapToDeliveryAddress(request.getDeliveryAddress()))
                .createdAt(LocalDateTime.now())
                .build();
        
        // Add order items
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName(itemRequest.getProductName())
                    .unitPrice(itemRequest.getUnitPrice())
                    .quantity(itemRequest.getQuantity())
                    .build();
            order.addOrderItem(orderItem);
        }
        
        // Calculate totals
        order.calculateTotals();
        
        return order;
    }
    
    private DeliveryAddress mapToDeliveryAddress(CreateOrderRequest.DeliveryAddressRequest request) {
        return DeliveryAddress.builder()
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .addressLine1(request.getAddressLine1())
                .ward(request.getWard())
                .district(request.getDistrict())
                .city(request.getCity())
                .build();
    }
    
    private String generateOrderCode() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .currency(order.getCurrency())
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .shippingFee(order.getShippingFee())
                .grandTotal(order.getGrandTotal())
                .note(order.getNote())
                .deliveryAddress(mapToDeliveryAddressResponse(order.getDeliveryAddress()))
                .orderItems(order.getOrderItems().stream()
                        .map(this::mapToOrderItemResponse)
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .build();
    }
    
    private OrderResponse.DeliveryAddressResponse mapToDeliveryAddressResponse(DeliveryAddress deliveryAddress) {
        return OrderResponse.DeliveryAddressResponse.builder()
                .receiverName(deliveryAddress.getReceiverName())
                .receiverPhone(deliveryAddress.getReceiverPhone())
                .addressLine1(deliveryAddress.getAddressLine1())
                .ward(deliveryAddress.getWard())
                .district(deliveryAddress.getDistrict())
                .city(deliveryAddress.getCity())
                .fullAddress(deliveryAddress.getFullAddress())
                .build();
    }
    
    private OrderResponse.OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return OrderResponse.OrderItemResponse.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .lineTotal(orderItem.getLineTotal())
                .build();
    }
}
