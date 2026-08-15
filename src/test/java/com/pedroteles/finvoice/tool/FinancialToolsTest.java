package com.pedroteles.finvoice.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pedroteles.finvoice.dto.CreateTransactionRequest;
import com.pedroteles.finvoice.dto.FinancialSummaryResponse;
import com.pedroteles.finvoice.dto.TransactionResponse;
import com.pedroteles.finvoice.enums.Category;
import com.pedroteles.finvoice.enums.TransactionType;
import com.pedroteles.finvoice.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinancialToolsTest {

    @Mock
    private TransactionService transactionService;

    private FinancialTools financialTools;

    @BeforeEach
    void setUp() {
        financialTools = new FinancialTools(transactionService);
    }

    @Test
    void shouldCreateExpenseUsingTransactionService() {
        when(transactionService.create(any(CreateTransactionRequest.class))).thenReturn(response(
                "Mercado",
                "45.00",
                TransactionType.EXPENSE,
                Category.FOOD
        ));

        String result = financialTools.createExpense("Mercado", new BigDecimal("45.00"), Category.FOOD);

        assertTrue(result.contains("Despesa registrada"));
        verify(transactionService).create(any(CreateTransactionRequest.class));
    }

    @Test
    void shouldCreateIncomeUsingTransactionService() {
        when(transactionService.create(any(CreateTransactionRequest.class))).thenReturn(response(
                "Salário",
                "1500.00",
                TransactionType.INCOME,
                Category.SALARY
        ));

        String result = financialTools.createIncome("Salário", new BigDecimal("1500.00"), Category.SALARY);

        assertTrue(result.contains("Receita registrada"));
        verify(transactionService).create(any(CreateTransactionRequest.class));
    }

    @Test
    void shouldReturnBalance() {
        when(transactionService.getBalance()).thenReturn(new BigDecimal("1455.00"));

        String result = financialTools.getBalance();

        assertEquals("Saldo atual: R$ 1455.00.", result);
    }

    @Test
    void shouldReturnSummaryByCategory() {
        FinancialSummaryResponse summary = new FinancialSummaryResponse(
                new BigDecimal("0.00"),
                new BigDecimal("45.00"),
                new BigDecimal("-45.00"),
                1,
                Category.FOOD
        );

        when(transactionService.getSummary(Category.FOOD)).thenReturn(summary);

        FinancialSummaryResponse response = financialTools.getSummaryByCategory(Category.FOOD);

        assertEquals(Category.FOOD, response.category());
        assertEquals(new BigDecimal("-45.00"), response.balance());
    }

    @Test
    void shouldReturnRecentTransactions() {
        List<TransactionResponse> transactions = List.of(response("Mercado", "45.00", TransactionType.EXPENSE, Category.FOOD));

        when(transactionService.getRecentTransactions()).thenReturn(transactions);

        List<TransactionResponse> result = financialTools.getRecentTransactions();

        assertEquals(1, result.size());
        assertEquals("Mercado", result.getFirst().description());
    }

    private TransactionResponse response(String description, String amount, TransactionType type, Category category) {
        return new TransactionResponse(1L, description, new BigDecimal(amount), type, category, LocalDateTime.now());
    }
}
