package com.pedroteles.finvoice.service;

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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TransactionService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        validate(request);

        Transaction transaction = new Transaction(
                request.description().trim(),
                normalize(request.amount()),
                request.type(),
                request.category(),
                LocalDateTime.now()
        );

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll() {
        return transactionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        return transactionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new TransactionNotFoundException(id);
        }

        transactionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance() {
        return summaryFrom(transactionRepository.findAll()).balance();
    }

    @Transactional(readOnly = true)
    public FinancialSummaryResponse getSummary(Category category) {
        List<Transaction> transactions = category == null
                ? transactionRepository.findAll()
                : transactionRepository.findByCategory(category);

        FinancialSummaryResponse summary = summaryFrom(transactions);

        return new FinancialSummaryResponse(
                summary.totalIncome(),
                summary.totalExpense(),
                summary.balance(),
                summary.transactionCount(),
                category
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions() {
        return transactionRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FinancialSummaryResponse summaryFrom(List<Transaction> transactions) {
        BigDecimal totalIncome = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(ZERO, BigDecimal::add);

        return new FinancialSummaryResponse(
                normalize(totalIncome),
                normalize(totalExpense),
                normalize(totalIncome.subtract(totalExpense)),
                transactions.size(),
                null
        );
    }

    private void validate(CreateTransactionRequest request) {
        if (request == null) {
            throw new InvalidTransactionException("Transação inválida.");
        }

        if (!StringUtils.hasText(request.description())) {
            throw new InvalidTransactionException("Descrição é obrigatória.");
        }

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Valor deve ser maior que zero.");
        }

        if (request.type() == null) {
            throw new InvalidTransactionException("Tipo é obrigatório.");
        }

        if (request.category() == null) {
            throw new InvalidTransactionException("Categoria é obrigatória.");
        }
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getCreatedAt()
        );
    }
}
