package model;


import java.util.ArrayList;
import java.util.List;

public class MonthlyBudget {
    private int id;
    private int year;
    private String monthName;
    private double initialBalance;
    private List<Expense> expenseList;


    public MonthlyBudget(int id, int year , String monthName, double monthlyBudget) {
        this.id = id;
        this.year = year;
        this.monthName = monthName;
        this.initialBalance = monthlyBudget;
        this.expenseList = new ArrayList<>();
    }

    public String getMonthName() {
        return monthName;
    }
    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public double getInitialBalance() {
        return initialBalance;
    }
    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public List<Expense> getExpenseList() {
        return expenseList;
    }

    public void addExpense(Expense expense) {
        expenseList.add(expense);
    }

    public void showExpenses() {
        for (Expense expense : expenseList) {
            System.out.println(expense);
        }
    }

    public String toString(){
        return year + "-" + monthName + " " + initialBalance;
    }

    public double getRemainingBalance() {
        double totalSpent = expenseList.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        return initialBalance - totalSpent;
    }

    public int getId() { return id; }

    public double getTotalByCategory(ExpenseCategory category) {
        return expenseList.stream().filter(e -> e.getCategory() == category)
                .mapToDouble(Expense::getAmount).sum();

    }
}
