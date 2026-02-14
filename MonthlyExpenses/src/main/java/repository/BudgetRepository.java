package repository;

import db.DatabaseConnection;
import model.Expense;
import model.ExpenseCategory;
import model.MonthlyBudget;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetRepository {

    public int saveBudget(MonthlyBudget budget) {
        String sql = "INSERT INTO monthly_budgets (year, month_name, initial_balance) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, 2026);
            pstmt.setString(2, budget.getMonthName());
            pstmt.setDouble(3, budget.getInitialBalance());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void saveExpense(Expense expense, int budgetId) {
        String sql = "INSERT INTO expenses (description, amount, date, budget_id,category) VALUES (?, ?, ?, ?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expense.getDescription());
            pstmt.setDouble(2, expense.getAmount());
            pstmt.setDate(3, java.sql.Date.valueOf(expense.getDate()));
            pstmt.setInt(4, budgetId);
            pstmt.setString(5, expense.getCategory().name()); // Dodajesz 5-ty parametr

            pstmt.executeUpdate();
            System.out.println("Wydatek '" + expense.getDescription() + "' zapisany w bazie!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearDatabase() {
        String sql = "TRUNCATE TABLE monthly_budgets RESTART IDENTITY CASCADE";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
             stmt.execute(sql);
        } catch (SQLException e) {
        }
    }

    public List<MonthlyBudget> getAllBudgets(){
        List<MonthlyBudget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM MONTHLY_BUDGETS";

        try (Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)){

            while(rs.next()){
                int id = rs.getInt("id");
                int year = rs.getInt("year");
                String month = rs.getString("month_name");
                Double initialBalance = rs.getDouble("initial_balance");

                MonthlyBudget budget = new MonthlyBudget(id,year, month, initialBalance);

                budgets.add(budget);

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return budgets;
    }

    public List<Expense> getExpensesByBudgetId(int budgetId) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE budget_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, budgetId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String description = rs.getString("description");
                    double amount = rs.getDouble("amount");
                    LocalDate date = rs.getDate("date").toLocalDate();
                    String categoryStr = rs.getString("category");
                    ExpenseCategory category = ExpenseCategory.valueOf(categoryStr);
                    expenses.add(new Expense(id,description, amount, date,category));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return expenses;
    }
}