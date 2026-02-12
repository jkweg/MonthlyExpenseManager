import model.MonthlyBudget;
import repository.BudgetRepository;

public class Program {

    public static void main(String[] args) {
        MonthlyBudget testowyBudzet = new MonthlyBudget(2026, "Luty", 5000.0);
        BudgetRepository budgetRepo = new BudgetRepository();
        System.out.println("Próba zapisu budżetu do bazy...");
        int generatedId = budgetRepo.saveBudget(testowyBudzet);
        if (generatedId != -1) {
            System.out.println("Sukces! Budżet zapisany. Nadane ID w bazie to: " + generatedId);
        } else {
            System.out.println("Błąd! Coś poszło nie tak. Sprawdź konsolę pod kątem błędów (Exception).");
        }
    }
}