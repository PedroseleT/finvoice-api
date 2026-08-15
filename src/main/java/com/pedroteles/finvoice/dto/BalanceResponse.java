package com.pedroteles.finvoice.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal balance
) {
}
