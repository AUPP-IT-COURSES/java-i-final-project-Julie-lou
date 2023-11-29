import javax.crypto.Cipher;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseTrackerApp extends JFrame{
    private List<Expense> expenses;
    private JComboBox<String> categoryCombobox;
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private Map<String, List<Expense>> expenseCategoryMap;

    public ExpenseTrackerApp(){
        expenses = new ArrayList<>();
        expenseCategoryMap = new HashMap<>();
        tableModel = new DefaultTableModel(new Object[]{"Category" ,"Description", "Amount"}, 0);

        setTitle("Expense Tracker App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900,600);
        setLayout(new BorderLayout());

        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.NORTH);

        expenseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane((expenseTable));
        add(scrollPane, BorderLayout.CENTER);


        JPanel reportPanel = createReportPanel();
        add(reportPanel, BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setBackground(Color.GRAY);
        JLabel categoryLabel = new JLabel("Category");
        categoryCombobox = new JComboBox<>();
        categoryCombobox.setBackground(Color.lightGray);
        categoryCombobox.addItem("All");
        categoryCombobox.addItem("Food");
        categoryCombobox.addItem("Transportation");
        categoryCombobox.addItem("Entertainment");
        categoryCombobox.addItem("Other");

        JLabel descriptionLabel = new JLabel("Description");
        JTextField descriptionTextField = new JTextField(20);

        JLabel amountLabel = new JLabel("Amount");
        JTextField amountTextField = new JTextField(10);

        JButton addButton = new JButton("Add Expense");
        addButton.setForeground(Color.red);
        addButton.addActionListener(new AddExpenseListener(categoryCombobox, descriptionTextField, amountTextField));

        inputPanel.add(categoryLabel);
        inputPanel.add(categoryCombobox);
        inputPanel.add(descriptionLabel);
        inputPanel.add(descriptionTextField);
        inputPanel.add(amountLabel);
        inputPanel.add(amountTextField);
        inputPanel.add(addButton);

        return inputPanel;

    }

    private  JPanel createReportPanel(){
        JPanel reportPanel = new JPanel (new FlowLayout());
        reportPanel.setBackground(Color.lightGray);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new SearchButtonListener());

        JLabel sortLabel = new JLabel("Sort By");
        JComboBox<String> sortCombobox = new JComboBox<>();
        sortCombobox.addItem("Category");
        sortCombobox.addItem("Amount");

        JButton sortButton = new JButton("Sort");
        sortButton.addActionListener(new SortButtonListener(sortCombobox));

        JButton totalExpenseButton = new JButton("Total Expense");
        totalExpenseButton.addActionListener(new TotalExpenseButtonListener());
        totalExpenseButton.setBackground(new Color(250,0,0));

        JButton generateReportButton = new JButton("Generate Report");
        generateReportButton.addActionListener(new GenerateReportButtonListener());

        reportPanel.add(searchButton);
        reportPanel.add(sortLabel);
        reportPanel.add(sortCombobox);
        reportPanel.add(sortButton);
        reportPanel.add(totalExpenseButton);
        reportPanel.add(generateReportButton);
        reportPanel.setVisible(true);

        return reportPanel;
    }

    private void updateExpenseList(){
        tableModel.setRowCount(0);

        String selectedCategory = categoryCombobox.getSelectedItem().toString();
        List<Expense> filteredExpense = expenses;

        if (!selectedCategory.equals("All")){
            filteredExpense = expenseCategoryMap.get(selectedCategory);
        }

        for(Expense expense : filteredExpense){
            tableModel.addRow(new Object[]{expense.getCategory(), expense.getDescription(), expense.getAmount()});
        }
    }

    private class AddExpenseListener implements ActionListener{
        private  JComboBox<String> categoryComboBox;
        private JTextField descriptioTextField;
        private  JTextField amountTextField;
        public AddExpenseListener(JComboBox<String> categoryCombobox, JTextField descriptionTextField, JTextField amountTextField) {
            this.categoryComboBox = categoryCombobox;
            this.descriptioTextField = descriptionTextField;
            this.amountTextField = amountTextField;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String category = categoryComboBox.getSelectedItem().toString();
            String description = descriptioTextField.getText();
            double amount = Double.parseDouble(amountTextField.getText());

            Expense expense = new Expense(category, description, amount);
            expenses.add(expense);

            if(expenseCategoryMap.containsKey(category)){
                expenseCategoryMap.get(category).add(expense);
            }
            else {
                List<Expense> categoryExpenses = new ArrayList<>();
                categoryExpenses.add(expense);
                expenseCategoryMap.put(category,categoryExpenses);
            }
            updateExpenseList();
            descriptioTextField.setText("");
            amountTextField.setText("");

        }
    }
    private class SearchButtonListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            String searchTerm = JOptionPane.showInputDialog(ExpenseTrackerApp.this,"Enter search term");
            if(searchTerm!=null){
                List<Expense> searchResults = expenses.stream()
                        .filter(expense -> expense.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
                tableModel.setRowCount(0);
                for (Expense expense : searchResults) {
                    tableModel.addRow(new Object[]{expense.getCategory(), expense.getDescription(), expense.getAmount()});
                }
            }
        }
    }
    private class SortButtonListener implements ActionListener{

        private JComboBox<String> sortComboBox;
        public  SortButtonListener(JComboBox<String> sortComboBox){this.sortComboBox = sortComboBox;}

        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedSort = sortComboBox.getSelectedItem().toString();

            if (selectedSort.equals("Category")){
                expenses.sort((e1, e2) -> e1.getCategory().compareToIgnoreCase(e2.getCategory()));
            } else if (selectedSort.equals("Amount")) {
                expenses.sort((e1, e2) -> Double.compare(e1.getAmount(), e2.getAmount()));
            }

            updateExpenseList();

        }
        }
        private class  TotalExpenseButtonListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                double totalExpense = expenses.stream()
                        .mapToDouble(Expense::getAmount)
                        .sum();
                JOptionPane.showMessageDialog(ExpenseTrackerApp.this, "Total Expense: $" + totalExpense);

            }
        }
        private class GenerateReportButtonListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder reportBuilder = new StringBuilder();

                for (Map.Entry<String, List<Expense>> entry : expenseCategoryMap.entrySet()){
                    String category = entry.getKey();
                    List<Expense> categoryExpenses = entry.getValue();
                    reportBuilder.append("Categroy: ").append(category).append("\n");

                    for(Expense expense : categoryExpenses) {
                        reportBuilder.append("Description: ").append(expense.getDescription()).append(", ")
                                .append("Amount: $").append(expense.getAmount()).append("\n");

                    }
                    reportBuilder.append("\n");
                }
                JTextArea reportTextArea = new JTextArea(reportBuilder.toString());
                reportTextArea.setEditable(false);

                JScrollPane reportScrollPane= new JScrollPane(reportTextArea);
                reportScrollPane.setPreferredSize(new Dimension(400, 300));

                JOptionPane.showMessageDialog(ExpenseTrackerApp.this, reportScrollPane, "Expense report", JOptionPane.PLAIN_MESSAGE);
            }
        }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->{
            ExpenseTrackerApp app = new ExpenseTrackerApp();
            app.setVisible(true);
        });
    }
}
class  Expense{
    private String category;
    private String description;
    private double amount;

    public Expense(String category, String description, double amount){
        this.category = category;
        this.description = description;
        this.amount = amount;
    }

    public String getCategory(){
        return category;
    }
    public String getDescription(){
        return  description;
    }
    public double getAmount(){
        return amount;
    }
}


