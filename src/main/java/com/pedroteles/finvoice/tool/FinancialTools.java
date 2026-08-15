package com.pedroteles.finvoice.tool;

import com.pedroteles.finvoice.dto.CreateTransactionRequest;
import com.pedroteles.finvoice.dto.FinancialSummaryResponse;
import com.pedroteles.finvoice.dto.TransactionResponse;
import com.pedroteles.finvoice.enums.Category;
import com.pedroteles.finvoice.enums.TransactionType;
import com.pedroteles.finvoice.service.TransactionService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class FinancialTools {

    private final TransactionService transactionService;

    public FinancialTools(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Tool(description = "Cria uma despesa financeira com descrição, valor positivo e categoria")
    public String createExpense(
            @ToolParam(description = "Descrição curta da despesa") String description,
            @ToolParam(description = "Valor positivo da despesa") BigDecimal amount,
            @ToolParam(description = "Categoria da despesa") Category category
    ) {
        TransactionResponse response = transactionService.create(new CreateTransactionRequest(
                description,
                amount,
                TransactionType.EXPENSE,
                category
        ));

        return "Despesa registrada: " + response.description() + " no valor de R$ " + response.amount() + ".";
    }

    @Tool(description = "Cria uma receita financeira com descrição, valor positivo e categoria")
    public String createIncome(
            @ToolParam(description = "Descrição curta da receita") String description,
            @ToolParam(description = "Valor positivo da receita") BigDecimal amount,
            @ToolParam(description = "Categoria da receita") Category category
    ) {
        TransactionResponse response = transactionService.create(new CreateTransactionRequest(
                description,
                amount,
                TransactionType.INCOME,
                category
        ));

        return "Receita registrada: " + response.description() + " no valor de R$ " + response.amount() + ".";
    }

    @Tool(description = "Consulta o saldo financeiro atual")
    public String getBalance() {
        return "Saldo atual: R$ " + transactionService.getBalance() + ".";
    }

    @Tool(description = "Consulta o resumo financeiro geral")
    public FinancialSummaryResponse getSummary() {
        return transactionService.getSummary(null);
    }

    @Tool(description = "Consulta o resumo financeiro filtrado por categoria")
    public FinancialSummaryResponse getSummaryByCategory(
            @ToolParam(description = "Categoria financeira") Category category
    ) {
        return transactionService.getSummary(category);
    }

    @Tool(description = "Consulta as últimas transações cadastradas")
    public List<TransactionResponse> getRecentTransactions() {
        return transactionService.getRecentTransactions();
    }
}
