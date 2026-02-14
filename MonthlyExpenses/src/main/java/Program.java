import model.Expense;
import model.ExpenseCategory;
import model.MonthlyBudget;
import repository.BudgetRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class Program {

    public static void main(String[] args) {
        BudgetRepository budgetRepo = new BudgetRepository();
        budgetRepo.clearDatabase();

        MonthlyBudget testowyBudzet = new MonthlyBudget(0,2026, "Luty", 5000.0);

        int idLutego = budgetRepo.saveBudget(testowyBudzet);
        Expense zakupy = new Expense(0,"Biedronka", 150.0, LocalDate.now(), ExpenseCategory.GROCERIES);
        Expense zakupy2 = new Expense(0,"Lidl", 150.0, LocalDate.now(),ExpenseCategory.GROCERIES);
        Expense zakupy3 = new Expense(0,"Lewiatan", 150.0, LocalDate.now(),ExpenseCategory.GROCERIES);

        Expense zakupy4 = new Expense(0,"House", 250.0, LocalDate.now(),ExpenseCategory.SHOPPING);
        Expense zakupy5 = new Expense(0,"H&M", 400.0, LocalDate.now(),ExpenseCategory.SHOPPING);
        budgetRepo.saveExpense(zakupy, idLutego);
        budgetRepo.saveExpense(zakupy2, idLutego);
        budgetRepo.saveExpense(zakupy3, idLutego);
        budgetRepo.saveExpense(zakupy4, idLutego);
        budgetRepo.saveExpense(zakupy5, idLutego);

        List<MonthlyBudget> budgets = budgetRepo.getAllBudgets();

        for(MonthlyBudget budget : budgets){
            System.out.println(budget.toString());
        }

        List<Expense> expenses = budgetRepo.getExpensesByBudgetId(idLutego);

        for(Expense expense : expenses){
            System.out.println(expense.toString());
        }

        System.out.println("-----------------");

        Map<ExpenseCategory, Double> report = budgetRepo.getCategoryReport(idLutego);

        for (Map.Entry<ExpenseCategory, Double> entry : report.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " zl");
        }


    }
}