package com.pedroteles.finvoice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pedroteles.finvoice.dto.CreateTransactionRequest;
import com.pedroteles.finvoice.dto.FinancialSummaryResponse;
import com.pedroteles.finvoice.dto.TransactionResponse;
import com.pedroteles.finvoice.entity.Transaction;
import com.pedroteles.finvoice.enums.Category;
import com.pedroteles.finvoice.enums.TransactionType;
import com.pedroteles.finvoice.exception.InvalidTransactionException;
import com.pedroteles.finvoice.exception.TransactionNotFoundException;
import com.pedroteles.finvoice.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository);
    }

    @Test
    void shouldCreateExpenseTransaction() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "Mercado",
                new BigDecimal("45.50"),
                TransactionType.EXPENSE,
                Category.FOOD
        );

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.create(request);

        assertEquals("Mercado", response.description());
        assertEquals(new BigDecimal("45.50"), response.amount());
        assertEquals(TransactionType.EXPENSE, response.type());
        assertEquals(Category.FOOD, response.category());
    }

    @Test
    void shouldRejectTransactionWithInvalidAmount() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "Mercado",
                BigDecimal.ZERO,
                TransactionType.EXPENSE,
                Category.FOOD
        );

        assertThrows(InvalidTransactionException.class, () -> transactionService.create(request));
    }

    @Test
    void shouldFindTransactionById() {
        Transaction transaction = transaction("Salário", "1500.00", TransactionType.INCOME, Category.SALARY);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.findById(1L);

        assertEquals("Salário", response.description());
        assertEquals(TransactionType.INCOME, response.type());
    }

    @Test
    void shouldThrowWhenTransactionDoesNotExist() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.findById(99L));
    }

    @Test
    void shouldCalculateBalance() {
        when(transactionRepository.findAll()).thenReturn(List.of(
                transaction("Salário", "1500.00", TransactionType.INCOME, Category.SALARY),
                transaction("Mercado", "45.00", TransactionType.EXPENSE, Category.FOOD),
                transaction("Curso", "100.00", TransactionType.EXPENSE, Category.EDUCATION)
        ));

        BigDecimal balance = transactionService.getBalance();

        assertEquals(new BigDecimal("1355.00"), balance);
    }

    @Test
    void shouldCalculateSummary() {
        when(transactionRepository.findAll()).thenReturn(List.of(
                transaction("Salário", "1500.00", TransactionType.INCOME, Category.SALARY),
                transaction("Mercado", "45.00", TransactionType.EXPENSE, Category.FOOD)
        ));

        FinancialSummaryResponse response = transactionService.getSummary(null);

        assertEquals(new BigDecimal("1500.00"), response.totalIncome());
        assertEquals(new BigDecimal("45.00"), response.totalExpense());
        assertEquals(new BigDecimal("1455.00"), response.balance());
        assertEquals(2, response.transactionCount());
    }

    @Test
    void shouldCalculateSummaryByCategory() {
        when(transactionRepository.findByCategory(Category.FOOD)).thenReturn(List.of(
                transaction("Mercado", "45.00", TransactionType.EXPENSE, Category.FOOD),
                transaction("Restaurante", "70.00", TransactionType.EXPENSE, Category.FOOD)
        ));

        FinancialSummaryResponse response = transactionService.getSummary(Category.FOOD);

        assertEquals(new BigDecimal("0.00"), response.totalIncome());
        assertEquals(new BigDecimal("115.00"), response.totalExpense());
        assertEquals(new BigDecimal("-115.00"), response.balance());
        assertEquals(Category.FOOD, response.category());
    }

    @Test
    void shouldDeleteExistingTransaction() {
        when(transactionRepository.existsById(1L)).thenReturn(true);

        transactionService.deleteById(1L);

        verify(transactionRepository).deleteById(1L);
    }

    private Transaction transaction(String description, String amount, TransactionType type, Category category) {
        return new Transaction(description, new BigDecimal(amount), type, category, LocalDateTime.now());
    }
}
