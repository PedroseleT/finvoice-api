package com.pedroteles.finvoice.controller;

import com.pedroteles.finvoice.dto.BalanceResponse;
import com.pedroteles.finvoice.dto.CreateTransactionRequest;
import com.pedroteles.finvoice.dto.FinancialSummaryResponse;
import com.pedroteles.finvoice.dto.TransactionResponse;
import com.pedroteles.finvoice.enums.Category;
import com.pedroteles.finvoice.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.create(request));
    }

    @GetMapping
    public List<TransactionResponse> findAll() {
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    public TransactionResponse findById(@PathVariable Long id) {
        return transactionService.findById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        transactionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/balance")
    public BalanceResponse getBalance() {
        return new BalanceResponse(transactionService.getBalance());
    }

    @GetMapping("/summary")
    public FinancialSummaryResponse getSummary(@RequestParam(required = false) Category category) {
        return transactionService.getSummary(category);
    }
}
