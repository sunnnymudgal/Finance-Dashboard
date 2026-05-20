package com.expensetracker.controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.expensetracker.entity.Expense;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.time.YearMonth;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import java.util.Comparator;
@Controller
public class HomeController {

    private final ExpenseRepository expenseRepository;

    public HomeController(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @GetMapping("/")
    public String home(Model model) {

        // ALL EXPENSES

        var expenses = expenseRepository.findAll();

        // TOTAL EXPENSES (ALL TIME)

        double totalExpenses = expenses.stream()

                .mapToDouble(Expense::getAmount)

                .sum();

        // MONTHLY EXPENSES

        double monthlyExpenses = expenses.stream()

                .filter(expense ->

                        expense.getDate() != null &&

                                YearMonth.from(
                                        expense.getDate()
                                ).equals(YearMonth.now())
                )

                .mapToDouble(Expense::getAmount)

                .sum();

        // YEARLY EXPENSES

        double yearlyExpenses = expenses.stream()

                .filter(expense ->

                        expense.getDate() != null &&

                                expense.getDate().getYear()
                                        == LocalDate.now().getYear()
                )

                .mapToDouble(Expense::getAmount)

                .sum();

        // TRANSACTION COUNT

        long transactionCount = expenses.size();

        // CATEGORY ANALYSIS

        Map<String, Double> categoryTotals =
                new HashMap<>();

        for (Expense expense : expenses) {

            categoryTotals.put(

                    expense.getCategory(),

                    categoryTotals.getOrDefault(
                            expense.getCategory(),
                            0.0
                    ) + expense.getAmount()
            );
        }

        // TOP CATEGORY

        String topCategory = "No Data";

        double highest = 0;

        for (Map.Entry<String, Double> entry :
                categoryTotals.entrySet()) {

            if (entry.getValue() > highest) {

                highest = entry.getValue();

                topCategory = entry.getKey();
            }
        }

        // AI INSIGHT

        String aiInsight;

        if (highest > 0) {

            aiInsight =
                    "You are spending most on "
                            + topCategory
                            + ". Total spending in this category is ₹"
                            + highest
                            + ". Consider optimizing this category to save more.";

        } else {

            aiInsight =
                    "Start adding expenses to unlock AI financial insights.";
        }

        // SEND DATA TO FRONTEND

        model.addAttribute(
                "expenses",
                expenses);

        model.addAttribute(
                "expense",
                new Expense());

        model.addAttribute(
                "totalExpenses",
                totalExpenses);

        model.addAttribute(
                "monthlyExpenses",
                monthlyExpenses);

        model.addAttribute(
                "yearlyExpenses",
                yearlyExpenses);

        model.addAttribute(
                "transactionCount",
                transactionCount);

        model.addAttribute(
                "aiInsight",
                aiInsight);

        return "index";
    }

    @GetMapping("/history")
    public String historyPage(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            String category,

            Model model) {

        int pageSize = 5;

        List<Expense> filteredExpenses;

        // SEARCH + CATEGORY

        boolean hasKeyword =
                keyword != null &&
                        !keyword.trim().isEmpty();

        boolean hasCategory =
                category != null &&
                        !category.trim().isEmpty();

        if (hasKeyword && hasCategory) {

            filteredExpenses =
                    expenseRepository
                            .findByTitleContainingIgnoreCaseAndCategory(
                                    keyword,
                                    category
                            );

        } else if (hasKeyword) {

            filteredExpenses =
                    expenseRepository
                            .findByTitleContainingIgnoreCase(
                                    keyword
                            );

        } else if (hasCategory) {

            filteredExpenses =
                    expenseRepository
                            .findByCategory(
                                    category
                            );

        } else {

            // NEWEST TO OLDEST

            filteredExpenses =
                    expenseRepository.findAll(
                            Sort.by(
                                    Sort.Direction.DESC,
                                    "date"
                            )
                    );
        }

        // SORT SEARCH/FILTER RESULTS ALSO

        filteredExpenses =
                filteredExpenses.stream()

                        .sorted(
                                Comparator.comparing(
                                        Expense::getDate
                                ).reversed()
                        )

                        .toList();

        // PAGINATION

        int start =
                Math.min(
                        page * pageSize,
                        filteredExpenses.size()
                );

        int end =
                Math.min(
                        start + pageSize,
                        filteredExpenses.size()
                );

        List<Expense> paginatedList =
                filteredExpenses.subList(
                        start,
                        end
                );

        Page<Expense> expensePage =
                new PageImpl<>(

                        paginatedList,

                        PageRequest.of(
                                page,
                                pageSize
                        ),

                        filteredExpenses.size()
                );

        // SEND DATA

        model.addAttribute(
                "expenses",
                expensePage.getContent());

        model.addAttribute(
                "currentPage",
                page);

        model.addAttribute(
                "totalPages",
                expensePage.getTotalPages());

        model.addAttribute(
                "keyword",
                keyword);

        model.addAttribute(
                "category",
                category);

        return "history";
    }

    @PostMapping("/saveExpense")
    public String saveExpense(@ModelAttribute Expense expense) {

        expense.setDate(LocalDate.now());

        expenseRepository.save(expense);

        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id) {

        expenseRepository.deleteById(id);

        return "redirect:/history";
    }

    @GetMapping("/charts")
    public String aiFinanceSystem(

            @RequestParam(defaultValue = "month")
            String filter,

            Model model) {

        List<Expense> expenses =
                expenseRepository.findAll();

        LocalDate now = LocalDate.now();

        // FILTER LOGIC

        expenses = expenses.stream()

                .filter(expense -> {

                    LocalDate date =
                            expense.getDate();

                    if (date == null)
                        return false;

                    switch (filter) {

                        case "day":

                            return date.equals(now);

                        case "week":

                            return date.isAfter(
                                    now.minusDays(7));

                        case "month":

                            return date.getMonth()
                                    .equals(now.getMonth())

                                    &&

                                    date.getYear()
                                            == now.getYear();

                        case "year":

                            return date.getYear()
                                    == now.getYear();

                        default:

                            return true;
                    }

                })

                .toList();

        // CATEGORY ANALYSIS

        Map<String, Double> categoryData =
                new HashMap<>();

        for (Expense expense : expenses) {

            categoryData.put(

                    expense.getCategory(),

                    categoryData.getOrDefault(
                            expense.getCategory(),
                            0.0
                    ) + expense.getAmount()
            );
        }

        // TOTAL

        double total = expenses.stream()

                .mapToDouble(Expense::getAmount)
                .sum();

        // TOP CATEGORY

        String topCategory = "No Data";

        double highest = 0;

        for (Map.Entry<String, Double> entry :
                categoryData.entrySet()) {

            if (entry.getValue() > highest) {

                highest = entry.getValue();

                topCategory = entry.getKey();
            }
        }

        // AI MESSAGE

        String aiMessage;

        if (highest > 0) {

            aiMessage =
                    "Your highest spending category is "
                            + topCategory
                            + ". You spent ₹"
                            + highest
                            + " during this "
                            + filter
                            + ".";

        } else {

            aiMessage =
                    "No expenses found for this "
                            + filter + ".";
        }

        // SEND TO FRONTEND

        model.addAttribute(
                "categoryLabels",
                categoryData.keySet());

        model.addAttribute(
                "categoryAmounts",
                categoryData.values());

        model.addAttribute(
                "aiMessage",
                aiMessage);

        model.addAttribute(
                "topCategory",
                topCategory);

        model.addAttribute(
                "total",
                total);

        model.addAttribute(
                "selectedFilter",
                filter);

        return "charts";
    }
}