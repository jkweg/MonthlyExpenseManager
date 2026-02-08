import model.Expense;
import model.MonthlyBudget;

import java.time.LocalDate;


public class Program {

    public static void main(){
        MonthlyBudget monthlyBudget = new MonthlyBudget(2026,"Luty" , 3000);
        Expense expense = new Expense("jedzenie", 300,LocalDate.now());
        Expense expense2 = new Expense("jedzenie2", 300,LocalDate.now());
        Expense expense3 = new Expense("jedzenie3", 300,LocalDate.now());
        monthlyBudget.addExpense(expense);
        System.out.println(monthlyBudget.getRemainingBalance());
        monthlyBudget.addExpense(expense2);
        System.out.println(monthlyBudget.getRemainingBalance());
        monthlyBudget.addExpense(expense3);
        System.out.println(monthlyBudget.getRemainingBalance());
        monthlyBudget.showExpenses();

        BudgetManager budgetManager = new BudgetManager();
        budgetManager.createNewBudget(monthlyBudget);

        budgetManager.showBudgets();
        budgetManager.showCurrentBudgetsStatus();
    }
}
