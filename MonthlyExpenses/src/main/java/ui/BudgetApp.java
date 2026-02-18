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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Expense;
import model.ExpenseCategory;
import model.MonthlyBudget;
import repository.BudgetRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

        pieChart = new PieChart();
        pieChart.setTitle("Struktura wydatków");

        summaryLabel = new Label("Wybierz budżet...");
        summaryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

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

        Label label = new Label("Wybierz budżet:");
        budgetSelector = new ComboBox<>();
        budgetSelector.setPrefWidth(250);

        budgetSelector.setOnAction(e -> {
            currentBudget = budgetSelector.getValue();
            refreshData();
        });

        Button btnNew = new Button("Utwórz nowy budżet");
        btnNew.setOnAction(e -> {
            MonthlyBudget mb = new MonthlyBudget(0, 2026, "Nowy Miesiąc", 5000.0);
            budgetRepo.saveBudget(mb);
            loadBudgets();
        });

        box.getChildren().addAll(label, budgetSelector, btnNew);
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
            pieData.add(new PieChart.Data(entry.getKey().name(), entry.getValue()));
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
}