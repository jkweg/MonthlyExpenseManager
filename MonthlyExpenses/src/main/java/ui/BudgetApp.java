package ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Expense;
import model.ExpenseCategory;
import model.MonthlyBudget;
import repository.BudgetRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import service.BudgetExporter;

public class BudgetApp extends Application {

    private final BudgetRepository budgetRepo = new BudgetRepository();

    private TableView<Expense> table;
    private PieChart pieChart;
    private ComboBox<MonthlyBudget> budgetSelector;
    private Label summaryLabel;

    private MonthlyBudget currentBudget;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Budżet Domowy - JavaFX");

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        HBox topMenu = createTopMenu();
        layout.setTop(topMenu);

        table = createTable();
        layout.setCenter(table);

        VBox rightPane = new VBox(15);
        rightPane.setPadding(new Insets(0, 0, 0, 15));

        rightPane.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        pieChart = new PieChart();
        pieChart.setTitle("Struktura wydatków");

        summaryLabel = new Label("Wybierz budżet...");
        summaryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        summaryLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        summaryLabel.setAlignment(javafx.geometry.Pos.CENTER);
        summaryLabel.setMaxWidth(Double.MAX_VALUE);

        rightPane.getChildren().addAll(pieChart, summaryLabel);
        layout.setRight(rightPane);

        HBox inputForm = createInputForm();
        layout.setBottom(inputForm);

        loadBudgets();

        Scene scene = new Scene(layout, 1100, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private HBox createTopMenu() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(0, 0, 15, 0));
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label label = new Label("Wybierz budżet:");

        budgetSelector = new ComboBox<>();
        budgetSelector.setPrefWidth(200);
        budgetSelector.setOnAction(e -> {
            currentBudget = budgetSelector.getValue();
            refreshData();
        });

        Button btnNew = new Button("Nowy");
        btnNew.setOnAction(e -> showNewBudgetDialog());

        Button btnEdit = new Button("Edytuj");
        btnEdit.setOnAction(e -> {
            if (currentBudget != null) editCurrentBudget();
            else showAlert("Wybierz budżet do edycji!");
        });

        Button btnDelete = new Button("Usuń");
        btnDelete.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-border-color: red;");
        btnDelete.setOnAction(e -> {
            if (currentBudget != null) deleteCurrentBudget();
            else showAlert("Wybierz budżet do usunięcia!");
        });

        Button btnExport = new Button("Eksport CSV");

        btnExport.setOnAction(e -> exportToCSV());

        box.getChildren().addAll(label, budgetSelector, btnNew, btnEdit, btnDelete, btnExport);
        return box;
    }

    private TableView<Expense> createTable() {
        TableView<Expense> table = new TableView<>();

        TableColumn<Expense, String> colDesc = new TableColumn<>("Opis");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Expense, Double> colAmount = new TableColumn<>("Kwota");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Expense, String> colCat = new TableColumn<>("Kategoria");
        colCat.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Expense, LocalDate> colDate = new TableColumn<>("Data");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(colDesc, colAmount, colCat, colDate);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Usuń zaznaczony");
        deleteItem.setOnAction(e -> {
            Expense selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                budgetRepo.deleteExpense(selected.getId());
                refreshData();
            }
        });
        contextMenu.getItems().add(deleteItem);
        table.setContextMenu(contextMenu);

        table.setRowFactory(tv -> {
            TableRow<Expense> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()) ) {
                    Expense rowData = row.getItem();
                    editExpenseDialog(rowData);
                }
            });
            return row;
        });

        return table;
    }

    private HBox createInputForm() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(15, 0, 0, 0));

        TextField descField = new TextField();
        descField.setPromptText("Opis (np. Zakupy)");

        TextField amountField = new TextField();
        amountField.setPromptText("Kwota (np. 150.50)");

        ComboBox<ExpenseCategory> catBox = new ComboBox<>();
        catBox.getItems().addAll(ExpenseCategory.values());
        catBox.setPromptText("Kategoria");
        catBox.getSelectionModel().selectFirst();

        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button btnAdd = new Button("Dodaj");
        btnAdd.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // Zielony przycisk

        btnAdd.setOnAction(e -> {
            try {
                if (currentBudget == null) {
                    showAlert("Wybierz budżet, aby dodać wydatek!");
                    return;
                }

                String desc = descField.getText();
                double amount = Double.parseDouble(amountField.getText().replace(",", "."));
                ExpenseCategory cat = catBox.getValue();
                LocalDate date = datePicker.getValue();

                Expense ex = new Expense(0, desc, amount, date, cat);
                budgetRepo.saveExpense(ex, currentBudget.getId());

                descField.clear();
                amountField.clear();
                refreshData();

            } catch (NumberFormatException ex) {
                showAlert("Błędna kwota! Użyj kropki, np. 12.50");
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Błąd dodawania: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(descField, amountField, catBox, datePicker, btnAdd);
        return box;
    }

    private void loadBudgets() {
        List<MonthlyBudget> budgets = budgetRepo.getAllBudgets();
        budgetSelector.getItems().setAll(budgets);

        if (!budgets.isEmpty()) {
            budgetSelector.getSelectionModel().selectLast();
            currentBudget = budgetSelector.getValue();
            refreshData();
        }
    }

    private void refreshData() {
        if (currentBudget == null) return;

        List<Expense> expenses = budgetRepo.getExpensesByBudgetId(currentBudget.getId());

        ObservableList<Expense> data = FXCollections.observableArrayList(expenses);
        table.setItems(data);

        double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double initial = currentBudget.getInitialBalance();
        double left = initial - totalSpent;

        summaryLabel.setText(String.format("Budżet: %.2f zł\nWydano: %.2f zł\nPozostało: %.2f zł", initial, totalSpent, left));

        if (left < 0) summaryLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
        else summaryLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 14px;");

        refreshChart();
    }

    private void refreshChart() {
        if (currentBudget == null) return;
        Map<ExpenseCategory, Double> report = budgetRepo.getCategoryReport(currentBudget.getId());

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<ExpenseCategory, Double> entry : report.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey().toString(), entry.getValue()));
        }
        pieChart.setData(pieData);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Uwaga");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showNewBudgetDialog() {
        Dialog<MonthlyBudget> dialog = new Dialog<>();
        dialog.setTitle("Nowy Budżet");
        dialog.setHeaderText("Podaj dane dla nowego miesiąca");

        ButtonType loginButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Spinner<Integer> yearSpinner = new Spinner<>(2020, 2030, LocalDate.now().getYear());

        String[] polishMonths = {
                "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
                "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
        };
        ComboBox<String> monthBox = new ComboBox<>(FXCollections.observableArrayList(polishMonths));
        monthBox.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);

        TextField amountField = new TextField();
        amountField.setPromptText("np. 5000.00");

        grid.add(new Label("Rok:"), 0, 0);
        grid.add(yearSpinner, 1, 0);
        grid.add(new Label("Miesiąc:"), 0, 1);
        grid.add(monthBox, 1, 1);
        grid.add(new Label("Budżet (PLN):"), 0, 2);
        grid.add(amountField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    int year = yearSpinner.getValue();
                    String month = monthBox.getValue();
                    double amount = Double.parseDouble(amountField.getText().replace(",", "."));

                    // ID jest 0, bo baza danych sama je nada
                    return new MonthlyBudget(0, year, month, amount);
                } catch (NumberFormatException e) {
                    showAlert("Błędna kwota budżetu!");
                    return null;
                }
            }
            return null;
        });

        Optional<MonthlyBudget> result = dialog.showAndWait();

        result.ifPresent(budget -> {
            budgetRepo.saveBudget(budget);
            loadBudgets();
        });
    }

    private void editCurrentBudget() {
        if (currentBudget == null) return;

        Dialog<MonthlyBudget> dialog = new Dialog<>();
        dialog.setTitle("Edycja Budżetu");
        dialog.setHeaderText("Edytuj dane budżetu");

        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Spinner<Integer> yearSpinner = new Spinner<>(2020, 2030, currentBudget.getYear());

        String[] polishMonths = {
                "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
                "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
        };
        ComboBox<String> monthBox = new ComboBox<>(FXCollections.observableArrayList(polishMonths));
        monthBox.setValue(currentBudget.getMonthName());

        TextField amountField = new TextField(String.valueOf(currentBudget.getInitialBalance()));

        grid.add(new Label("Rok:"), 0, 0);
        grid.add(yearSpinner, 1, 0);
        grid.add(new Label("Miesiąc:"), 0, 1);
        grid.add(monthBox, 1, 1);
        grid.add(new Label("Budżet (PLN):"), 0, 2);
        grid.add(amountField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    currentBudget.setYear(yearSpinner.getValue());
                    currentBudget.setMonthName(monthBox.getValue());
                    currentBudget.setInitialBalance(Double.parseDouble(amountField.getText().replace(",", ".")));
                    return currentBudget;
                } catch (NumberFormatException e) {
                    showAlert("Błędna kwota!");
                    return null;
                }
            }
            return null;
        });

        Optional<MonthlyBudget> result = dialog.showAndWait();
        result.ifPresent(budget -> {
            budgetRepo.updateBudget(budget);
            loadBudgets();
        });
    }

    private void deleteCurrentBudget() {
        if (currentBudget == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie usunięcia");
        alert.setHeaderText("Czy na pewno chcesz usunąć budżet: " + currentBudget.toString() + "?");
        alert.setContentText("Zostaną usunięte również WSZYSTKIE wydatki z tego miesiąca!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            budgetRepo.deleteBudget(currentBudget.getId());
            currentBudget = null;
            loadBudgets();

            if (budgetSelector.getItems().isEmpty()) {
                table.getItems().clear();
                summaryLabel.setText("");
                pieChart.setData(FXCollections.observableArrayList());
            }
        }
    }

    private void editExpenseDialog(Expense expense) {
        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle("Edycja Wydatku");
        dialog.setHeaderText("Edytuj szczegóły wydatku");

        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField descField = new TextField(expense.getDescription());
        TextField amountField = new TextField(String.valueOf(expense.getAmount()));

        ComboBox<ExpenseCategory> catBox = new ComboBox<>();
        catBox.getItems().addAll(ExpenseCategory.values());
        catBox.setValue(expense.getCategory());

        DatePicker datePicker = new DatePicker(expense.getDate());

        grid.add(new Label("Opis:"), 0, 0);
        grid.add(descField, 1, 0);
        grid.add(new Label("Kwota:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Kategoria:"), 0, 2);
        grid.add(catBox, 1, 2);
        grid.add(new Label("Data:"), 0, 3);
        grid.add(datePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String newDesc = descField.getText();
                    double newAmount = Double.parseDouble(amountField.getText().replace(",", "."));
                    ExpenseCategory newCat = catBox.getValue();
                    LocalDate newDate = datePicker.getValue();

                    return new Expense(expense.getId(), newDesc, newAmount, newDate, newCat);
                } catch (NumberFormatException e) {
                    showAlert("Błędna kwota!");
                    return null;
                }
            }
            return null;
        });

        Optional<Expense> result = dialog.showAndWait();
        result.ifPresent(updatedExpense -> {
            budgetRepo.updateExpense(updatedExpense);
            refreshData();
        });
    }

    private void exportToCSV() {
        if (currentBudget == null) {
            showAlert("Wybierz budżet, aby go wyeksportować!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz budżet jako CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki CSV (*.csv)", "*.csv"));

        String suggestedName = "Budzet_" + currentBudget.getMonthName() + "_" + currentBudget.getYear();
        fileChooser.setInitialFileName(suggestedName);

        File file = fileChooser.showSaveDialog(table.getScene().getWindow());

        if (file != null) {
            try {
                List<Expense> expenses = budgetRepo.getExpensesByBudgetId(currentBudget.getId());

                service.BudgetExporter.saveExpensesToCSV(file, expenses);


                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sukces");
                alert.setHeaderText(null);
                alert.setContentText("Plik został pomyślnie zapisany:\n" + file.getAbsolutePath());
                alert.showAndWait();

            } catch (IOException e) {
                showAlert("Błąd zapisu pliku: " + e.getMessage());
            }
        }
    }
}