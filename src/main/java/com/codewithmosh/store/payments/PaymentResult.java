package com.codewithmosh.store.payments;

import com.codewithmosh.store.orders.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentResult {
    private Long orderId;
    private OrderStatus orderStatus;
}
