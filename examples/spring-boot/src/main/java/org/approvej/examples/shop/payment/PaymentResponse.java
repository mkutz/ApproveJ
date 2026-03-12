package org.approvej.examples.shop.payment;

import java.time.Instant;

public record PaymentResponse(String paymentId, String status, Instant processedAt) {}
