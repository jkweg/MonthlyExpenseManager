package model;

import java.time.LocalDate;

public class Expense {

    private int id;
    private double amount;
    private LocalDate date;
    private String description;

    public Expense(int id , String description,double money, LocalDate date) {
        this.id = id;
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

    public int getId() { return id; }
}
