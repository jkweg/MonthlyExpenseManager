package repository;

import db.DatabaseConnection;
import model.MonthlyBudget;
import java.sql.*;

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
}