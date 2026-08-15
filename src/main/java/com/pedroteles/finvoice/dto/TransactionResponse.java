package com.pedroteles.finvoice.dto;

import com.pedroteles.finvoice.enums.Category;
import com.pedroteles.finvoice.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String description,
        BigDecimal amount,
        TransactionType type,
        Category category,
        LocalDateTime createdAt
) {
}
