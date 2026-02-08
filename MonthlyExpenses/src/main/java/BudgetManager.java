import model.MonthlyBudget;

import java.util.HashMap;
import java.util.Map;

public class BudgetManager {

    private Map<String, MonthlyBudget> budgets;

    public BudgetManager() {
        budgets = new HashMap<>();

    }
    public void createNewBudget(MonthlyBudget budget) {
        budgets.put(budget.toString(), budget);
    }

    public MonthlyBudget getBudget(String budgetName) {
        return budgets.get(budgetName);
    }

    public Map<String, MonthlyBudget> getBudgets() {
        return budgets;
    }

    public void setBudgets(Map<String, MonthlyBudget> budgets) {
        this.budgets = budgets;
    }

    public void showBudgets() {
        for (Map.Entry<String, MonthlyBudget> entry : budgets.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue().getInitialBalance());
        }
    }

    public void showCurrentBudgetsStatus(){
        for (Map.Entry<String, MonthlyBudget> entry : budgets.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue().getRemainingBalance());
        }
    }

}
