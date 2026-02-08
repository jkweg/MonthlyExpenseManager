package model;

import java.time.LocalDate;

public class Expense {

    private double amount;
    private LocalDate date;
    private String description;

    public Expense(String description,double money, LocalDate date) {
        this.amount = money;
        this.date = date;
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description + " | " + amount + " | " + date;
    }
}
