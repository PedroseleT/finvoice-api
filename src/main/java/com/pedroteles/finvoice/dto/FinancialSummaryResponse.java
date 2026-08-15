package com.pedroteles.finvoice.dto;

import com.pedroteles.finvoice.enums.Category;
import java.math.BigDecimal;

public record FinancialSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        long transactionCount,
        Category category
) {
}
