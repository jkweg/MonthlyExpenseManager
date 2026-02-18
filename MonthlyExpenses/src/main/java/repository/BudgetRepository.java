package repository;

import db.DatabaseConnection;
import model.Expense;
import model.ExpenseCategory;
import model.MonthlyBudget;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<ExpenseCategory, Double> getCategoryReport(int budgetId) {
        Map<ExpenseCategory, Double> report = new HashMap<>();
        String sql = "SELECT category, SUM(amount) AS total FROM expenses WHERE budget_id = ? GROUP BY category";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, budgetId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ExpenseCategory cat = ExpenseCategory.valueOf(rs.getString("category"));
                double sum = rs.getDouble("total");
                report.put(cat, sum);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public void deleteExpense(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Wydatek o ID " + id + " został usunięty.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateExpense(Expense expense) {

        String sql = "UPDATE expenses SET description = ?, amount = ?, date = ?, category = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expense.getDescription());
            pstmt.setDouble(2, expense.getAmount());
            pstmt.setDate(3, java.sql.Date.valueOf(expense.getDate()));
            pstmt.setString(4, expense.getCategory().name());
            pstmt.setInt(5, expense.getId());

            pstmt.executeUpdate();
            System.out.println("Wydatek o ID " + expense.getId() + " został zaktualizowany.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBudget(MonthlyBudget budget) {
        String sql = "UPDATE monthly_budgets SET year = ?, month_name = ?, initial_balance = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, budget.getYear());
            pstmt.setString(2, budget.getMonthName());
            pstmt.setDouble(3, budget.getInitialBalance());
            pstmt.setInt(4, budget.getId());

            pstmt.executeUpdate();
            System.out.println("Budżet zaktualizowany: " + budget);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteBudget(int budgetId) {
        String deleteExpensesSql = "DELETE FROM expenses WHERE budget_id = ?";
        String deleteBudgetSql = "DELETE FROM monthly_budgets WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(deleteExpensesSql)) {
                pstmt.setInt(1, budgetId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(deleteBudgetSql)) {
                pstmt.setInt(1, budgetId);
                pstmt.executeUpdate();
            }

            System.out.println("Budżet o ID " + budgetId + " został usunięty.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}