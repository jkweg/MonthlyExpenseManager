package model;

public enum ExpenseCategory {
    GROCERIES("Artykuły spożywcze"),
    EATING_OUT("Jedzenie na mieście"),
    HOUSING("Mieszkanie / Rachunki"),
    TRANSPORT("Transport / Paliwo"),
    ENTERTAINMENT("Rozrywka"),
    SHOPPING("Zakupy / Ubrania"),
    EDUCATION("Edukacja"),
    HEALTH("Zdrowie"),
    OTHER("Inne");

    private final String displayName;

    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}