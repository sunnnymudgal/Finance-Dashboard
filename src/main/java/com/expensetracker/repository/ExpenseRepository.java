package com.expensetracker.repository;

import com.expensetracker.entity.Expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {

    // SEARCH BY TITLE

    List<Expense>
    findByTitleContainingIgnoreCase(
            String keyword);

    // FILTER BY CATEGORY

    List<Expense>
    findByCategory(
            String category);

    // FILTER BY TYPE

    List<Expense>
    findByType(
            String type);

    // SEARCH + CATEGORY

    List<Expense>
    findByTitleContainingIgnoreCaseAndCategory(

            String keyword,

            String category
    );

    // SEARCH + TYPE

    List<Expense>
    findByTitleContainingIgnoreCaseAndType(

            String keyword,

            String type
    );

    // CATEGORY + TYPE

    List<Expense>
    findByCategoryAndType(

            String category,

            String type
    );

    // SEARCH + CATEGORY + TYPE

    List<Expense>
    findByTitleContainingIgnoreCaseAndCategoryAndType(

            String keyword,

            String category,

            String type
    );
}