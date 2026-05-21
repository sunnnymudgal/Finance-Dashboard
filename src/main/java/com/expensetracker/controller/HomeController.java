package com.expensetracker.controller;

import com.expensetracker.entity.Expense;
import com.expensetracker.repository.ExpenseRepository;

import com.lowagie.text.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.YearMonth;

import jakarta.servlet.http.HttpServletResponse;

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;

import java.util.*;
import java.util.List;

@Controller
public class HomeController {

    private final ExpenseRepository expenseRepository;

    public HomeController(
            ExpenseRepository expenseRepository) {

        this.expenseRepository =
                expenseRepository;
    }

    // DASHBOARD

    @GetMapping("/")
    public String home(Model model) {

        List<Expense> expenses =
                expenseRepository.findAll();

        // TOTAL INCOME

        double totalIncome = expenses.stream()

                .filter(expense ->
                        "Income".equals(
                                expense.getType()))

                .mapToDouble(
                        Expense::getAmount)

                .sum();

        // TOTAL EXPENSE

        double totalExpense = expenses.stream()

                .filter(expense ->
                        "Expense".equals(
                                expense.getType()))

                .mapToDouble(
                        Expense::getAmount)

                .sum();

        // BALANCE

        double balance =
                totalIncome - totalExpense;

        // MONTHLY EXPENSES

        double monthlyExpenses = expenses.stream()

                .filter(expense ->

                        expense.getDate() != null &&

                                "Expense".equals(
                                        expense.getType()) &&

                                YearMonth.from(
                                                expense.getDate())

                                        .equals(
                                                YearMonth.now())
                )

                .mapToDouble(
                        Expense::getAmount)

                .sum();

        // YEARLY EXPENSES

        double yearlyExpenses = expenses.stream()

                .filter(expense ->

                        expense.getDate() != null &&

                                "Expense".equals(
                                        expense.getType()) &&

                                expense.getDate()
                                        .getYear()

                                        == LocalDate.now()
                                        .getYear()
                )

                .mapToDouble(
                        Expense::getAmount)

                .sum();

        // TRANSACTIONS

        long transactionCount =
                expenses.size();

        // CATEGORY ANALYSIS

        Map<String, Double> categoryTotals =
                new HashMap<>();

        for (Expense expense : expenses) {

            if ("Expense".equals(
                    expense.getType())) {

                categoryTotals.put(

                        expense.getCategory(),

                        categoryTotals.getOrDefault(
                                expense.getCategory(),
                                0.0)

                                + expense.getAmount()
                );
            }
        }

        // TOP CATEGORY

        String topCategory =
                "No Data";

        double highest = 0;

        for (Map.Entry<String, Double> entry :
                categoryTotals.entrySet()) {

            if (entry.getValue() > highest) {

                highest =
                        entry.getValue();

                topCategory =
                        entry.getKey();
            }
        }

        // FINANCIAL INSIGHT

        String aiInsight;

        if (balance > 0) {

            aiInsight =
                    "Your current balance is ₹"
                            + balance
                            + ". Your income is greater than your expenses. "
                            + "Highest expense category is "
                            + topCategory + ".";

        } else {

            aiInsight =
                    "Your expenses are exceeding your income. "
                            + "Try reducing spending in "
                            + topCategory + ".";
        }

        // SEND TO FRONTEND

        model.addAttribute(
                "expenses",
                expenses);

        model.addAttribute(
                "expense",
                new Expense());

        model.addAttribute(
                "totalIncome",
                totalIncome);

        model.addAttribute(
                "totalExpense",
                totalExpense);

        model.addAttribute(
                "balance",
                balance);

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

    // HISTORY

    @GetMapping("/history")
    public String historyPage(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            String category,

            @RequestParam(required = false)
            String type,
            @RequestParam(required = false)
            LocalDate startDate,

            @RequestParam(required = false)
            LocalDate endDate,
            Model model) {

        int pageSize = 5;

        List<Expense> filteredExpenses =
                expenseRepository.findAll();

        // SEARCH

        if (keyword != null &&
                !keyword.isEmpty()) {

            filteredExpenses =
                    filteredExpenses.stream()

                            .filter(expense ->

                                    expense.getTitle()

                                            .toLowerCase()

                                            .contains(
                                                    keyword.toLowerCase()))

                            .toList();
        }

        // CATEGORY

        if (category != null &&
                !category.isEmpty()) {

            filteredExpenses =
                    filteredExpenses.stream()

                            .filter(expense ->

                                    expense.getCategory()

                                            .equals(category))

                            .toList();
        }

        // TYPE

        if (type != null &&
                !type.isEmpty()) {

            filteredExpenses =
                    filteredExpenses.stream()

                            .filter(expense ->

                                    expense.getType()

                                            .equals(type))

                            .toList();
        }
        if (startDate != null &&
                endDate != null) {

            filteredExpenses =
                    filteredExpenses.stream()

                            .filter(expense ->

                                    !expense.getDate()
                                            .isBefore(startDate)

                                            &&

                                            !expense.getDate()
                                                    .isAfter(endDate)
                            )

                            .toList();
        }

        // SORT

        filteredExpenses =
                filteredExpenses.stream()

                        .sorted(

                                Comparator.comparing(
                                                Expense::getDate)

                                        .reversed())

                        .toList();

        // PAGINATION

        int start =
                Math.min(
                        page * pageSize,
                        filteredExpenses.size());

        int end =
                Math.min(
                        start + pageSize,
                        filteredExpenses.size());

        List<Expense> paginatedList =
                filteredExpenses.subList(
                        start,
                        end);

        Page<Expense> expensePage =
                new PageImpl<>(

                        paginatedList,

                        PageRequest.of(
                                page,
                                pageSize),

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
                "startDate",
                startDate);

        model.addAttribute(
                "endDate",
                endDate);
        model.addAttribute(
                "totalPages",
                expensePage.getTotalPages());

        model.addAttribute(
                "keyword",
                keyword);
        model.addAttribute(
                "type",
                type);
        model.addAttribute(
                "category",
                category);

        model.addAttribute(
                "type",
                type);

        return "history";
    }

    // SAVE

    @PostMapping("/saveExpense")
    public String saveExpense(

            @ModelAttribute Expense expense) {

        if (expense.getDate() == null) {

            expense.setDate(
                    LocalDate.now());
        }

        expenseRepository.save(expense);

        return "redirect:/";
    }

    // DELETE

    @GetMapping("/delete/{id}")
    public String deleteExpense(
            @PathVariable Long id) {

        expenseRepository.deleteById(id);

        return "redirect:/history";
    }

    // CHARTS
    @GetMapping("/charts")
    public String charts(

            @RequestParam(defaultValue = "month")
            String filter,

            @RequestParam(defaultValue = "Expense")
            String type,

            Model model) {

        List<Expense> expenses =
                expenseRepository.findAll();

        LocalDate now = LocalDate.now();

        // FILTER BY TYPE + DATE

        List<Expense> filteredExpenses =
                expenses.stream()

                        .filter(expense -> {

                            // TYPE FILTER

                            if (!expense.getType()
                                    .equalsIgnoreCase(type)) {

                                return false;
                            }

                            LocalDate date =
                                    expense.getDate();

                            if (date == null)
                                return false;

                            // DATE FILTER

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

        for (Expense expense : filteredExpenses) {

            categoryData.put(

                    expense.getCategory(),

                    categoryData.getOrDefault(
                            expense.getCategory(),
                            0.0
                    ) + expense.getAmount()
            );
        }

        // TOTALS

        double totalIncome = expenses.stream()

                .filter(expense ->
                        "Income".equalsIgnoreCase(
                                expense.getType()))

                .mapToDouble(Expense::getAmount)

                .sum();

        double totalExpense = expenses.stream()

                .filter(expense ->
                        "Expense".equalsIgnoreCase(
                                expense.getType()))

                .mapToDouble(Expense::getAmount)

                .sum();

        double balance =
                totalIncome - totalExpense;

        // FILTERED TOTAL

        double total = filteredExpenses.stream()

                .mapToDouble(Expense::getAmount)

                .sum();

        // TOP CATEGORY

        String topCategory = "No Data";

        double highest = 0;

        for (Map.Entry<String, Double> entry :
                categoryData.entrySet()) {

            if (entry.getValue() > highest) {

                highest =
                        entry.getValue();

                topCategory =
                        entry.getKey();
            }
        }

        // INSIGHT

        String aiMessage;

        if (filteredExpenses.isEmpty()) {

            aiMessage =
                    "No " + type.toLowerCase()
                            + " data found for this "
                            + filter + ".";

        } else {

            aiMessage =
                    "Your highest "
                            + type.toLowerCase()
                            + " category during this "
                            + filter
                            + " is "
                            + topCategory
                            + " with ₹"
                            + highest + ".";
        }

        // SEND DATA

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

        model.addAttribute(
                "selectedType",
                type);

        model.addAttribute(
                "totalIncome",
                totalIncome);

        model.addAttribute(
                "totalExpense",
                totalExpense);

        model.addAttribute(
                "balance",
                balance);

        return "charts";
    }

    @GetMapping("/download-report")
    public void downloadReport(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            String category,

            @RequestParam(required = false)
            String type,

            @RequestParam(required = false)
            LocalDate startDate,

            @RequestParam(required = false)
            LocalDate endDate,

            HttpServletResponse response)

            throws Exception {

        response.setContentType(
                "application/pdf");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=finance-report.pdf");

        // GET ALL DATA

        List<Expense> expenses =
                expenseRepository.findAll();

        // FILTER FLAGS

        boolean hasKeyword =
                keyword != null &&
                        !keyword.trim().isEmpty();

        boolean hasCategory =
                category != null &&
                        !category.trim().isEmpty();

        boolean hasType =
                type != null &&
                        !type.trim().isEmpty();

        // SEARCH FILTER

        if (hasKeyword) {

            expenses = expenses.stream()

                    .filter(expense ->

                            expense.getTitle()

                                    .toLowerCase()

                                    .contains(

                                            keyword.toLowerCase()
                                    )
                    )

                    .toList();
        }

        // CATEGORY FILTER

        if (hasCategory) {

            expenses = expenses.stream()

                    .filter(expense ->

                            expense.getCategory()

                                    .equalsIgnoreCase(
                                            category
                                    )
                    )

                    .toList();
        }

        // TYPE FILTER

        if (hasType) {

            expenses = expenses.stream()

                    .filter(expense ->

                            expense.getType()

                                    .equalsIgnoreCase(
                                            type
                                    )
                    )

                    .toList();
        }

        // DATE FILTER

        if (startDate != null &&
                endDate != null) {

            expenses = expenses.stream()

                    .filter(expense ->

                            !expense.getDate()
                                    .isBefore(startDate)

                                    &&

                                    !expense.getDate()
                                            .isAfter(endDate)
                    )

                    .toList();
        }

        // TOTAL INCOME

        double totalIncome = expenses.stream()

                .filter(expense ->

                        "Income".equalsIgnoreCase(
                                expense.getType()
                        )
                )

                .mapToDouble(Expense::getAmount)

                .sum();

        // TOTAL EXPENSE

        double totalExpense = expenses.stream()

                .filter(expense ->

                        "Expense".equalsIgnoreCase(
                                expense.getType()
                        )
                )

                .mapToDouble(Expense::getAmount)

                .sum();

        // BALANCE

        double balance =
                totalIncome - totalExpense;

        // PDF DOCUMENT

        Document document =
                new Document();

        PdfWriter.getInstance(
                document,
                response.getOutputStream());

        document.open();

        // TITLE

        Font titleFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        24,
                        Color.BLACK);

        Paragraph title =
                new Paragraph(
                        "Finance Dashboard Report",
                        titleFont);

        title.setSpacingAfter(20);

        document.add(title);

        // SUMMARY

        Font summaryFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        13);

        document.add(new Paragraph(
                "Total Income: ₹ "
                        + totalIncome,
                summaryFont));

        document.add(new Paragraph(
                "Total Expenses: ₹ "
                        + totalExpense,
                summaryFont));

        document.add(new Paragraph(
                "Current Balance: ₹ "
                        + balance,
                summaryFont));

        document.add(new Paragraph(" "));

        // TABLE

        PdfPTable table =
                new PdfPTable(5);

        table.setWidthPercentage(100);

        table.setSpacingBefore(10);

        // HEADERS

        String[] headers = {

                "Title",
                "Type",
                "Category",
                "Amount",
                "Date"
        };

        for (String header : headers) {

            PdfPCell cell =
                    new PdfPCell();

            cell.setBackgroundColor(
                    new Color(230,230,230));

            cell.setPadding(8);

            cell.setPhrase(
                    new Phrase(header));

            table.addCell(cell);
        }

        // TABLE DATA

        for (Expense expense : expenses) {

            table.addCell(
                    expense.getTitle());

            table.addCell(
                    expense.getType());

            table.addCell(
                    expense.getCategory());

            table.addCell(
                    "₹ " + expense.getAmount());

            table.addCell(
                    expense.getDate()
                            .toString());
        }

        document.add(table);

        document.close();
    }
}