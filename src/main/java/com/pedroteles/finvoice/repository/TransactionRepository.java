package com.pedroteles.finvoice.repository;

import com.pedroteles.finvoice.entity.Transaction;
import com.pedroteles.finvoice.enums.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCategory(Category category);

    List<Transaction> findTop5ByOrderByCreatedAtDesc();
}
