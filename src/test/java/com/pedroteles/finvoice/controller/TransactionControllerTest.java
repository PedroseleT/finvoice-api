package com.pedroteles.finvoice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedroteles.finvoice.dto.CreateTransactionRequest;
import com.pedroteles.finvoice.dto.FinancialSummaryResponse;
import com.pedroteles.finvoice.dto.TransactionResponse;
import com.pedroteles.finvoice.enums.Category;
import com.pedroteles.finvoice.enums.TransactionType;
import com.pedroteles.finvoice.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void shouldCreateTransaction() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "Mercado",
                new BigDecimal("45.00"),
                TransactionType.EXPENSE,
                Category.FOOD
        );

        TransactionResponse response = new TransactionResponse(
                1L,
                "Mercado",
                new BigDecimal("45.00"),
                TransactionType.EXPENSE,
                Category.FOOD,
                LocalDateTime.now()
        );

        when(transactionService.create(any(CreateTransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Mercado"))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.category").value("FOOD"));
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsInvalid() throws Exception {
        String body = """
                {
                  "description": "Mercado",
                  "amount": 0,
                  "type": "EXPENSE",
                  "category": "FOOD"
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBalance() throws Exception {
        when(transactionService.getBalance()).thenReturn(new BigDecimal("1455.00"));

        mockMvc.perform(get("/transactions/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1455.00));
    }

    @Test
    void shouldReturnSummary() throws Exception {
        when(transactionService.getSummary(Category.FOOD)).thenReturn(new FinancialSummaryResponse(
                new BigDecimal("0.00"),
                new BigDecimal("45.00"),
                new BigDecimal("-45.00"),
                1,
                Category.FOOD
        ));

        mockMvc.perform(get("/transactions/summary").param("category", "FOOD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpense").value(45.00))
                .andExpect(jsonPath("$.balance").value(-45.00))
                .andExpect(jsonPath("$.category").value("FOOD"));
    }

    @Test
    void shouldDeleteTransaction() throws Exception {
        mockMvc.perform(delete("/transactions/1"))
                .andExpect(status().isNoContent());
    }
}
