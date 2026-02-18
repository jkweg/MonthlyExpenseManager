package service;

import model.Expense;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class BudgetExporter {

    public static void saveExpensesToCSV(File file, List<Expense> expenses) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {

            writer.println("ID;Data;Opis;Kategoria;Kwota");

            for (Expense ex : expenses) {
                String amountStr = String.format("%.2f", ex.getAmount());

                String line = String.format("%d;%s;%s;%s;%s",
                        ex.getId(),
                        ex.getDate(),
                        ex.getDescription(),
                        ex.getCategory(),
                        amountStr
                );
                writer.println(line);
            }
        }
    }
}