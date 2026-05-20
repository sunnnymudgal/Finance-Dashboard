package com.expensetracker.repository;

import com.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {

    List<Expense> findByTitleContainingIgnoreCase(
            String keyword);

    List<Expense> findByCategory(
            String category);

    List<Expense> findByTitleContainingIgnoreCaseAndCategory(
            String keyword,
            String category);
}