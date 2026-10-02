package view;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import dao.ProductDAO;
import model.Product;

public class MainFrame extends JFrame {

    private ProductDAO productDAO = new ProductDAO();
    private DefaultTableModel summaryModel;
    private DefaultTableModel historyModel;
    private JComboBox<Product> comboProducts;
    private JTabbedPane tabbedPane;

    // Palette de couleurs modernes (Style Dashboard SaaS)
    private final Color COLOR_BG = Color.decode("#111214");         // Fond principal très sombre
    private final Color COLOR_SURFACE = Color.decode("#1A1C1E");    // Fond des cartes et composants
    private final Color COLOR_ACCENT = Color.decode("#6366F1");     // Indigo moderne pour la sélection
    private final Color COLOR_TEXT_MAIN = Color.decode("#F3F4F6");  // Blanc doux pour le texte principal
    private final Color COLOR_TEXT_MUTED = Color.decode("#9CA3AF"); // Gris pour les titres secondaires
    private final Color COLOR_GREEN = Color.decode("#10B981");      // Vert émeraude pour les montants
    private final Color COLOR_BORDER = Color.decode("#2A2D31");     // Bordure très discrète

    public MainFrame() {
        setTitle("Stock and Sales Management -  Manager Application");
        setSize(1150, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Configuration du conteneur d'onglets
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabbedPane.addTab(" Dashboard ", createDashboardPanel());
        tabbedPane.addTab(" Stock Management ", createStockPanel());
        tabbedPane.addTab(" Record a Sale ", createSalePanel());
        tabbedPane.addTab(" Sales History ", createHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Barre d'état en bas
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        statusPanel.setBackground(COLOR_SURFACE);
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));
        JLabel statusLabel = new JLabel("🟢 Connected to MariaDB - stock_management");
        statusLabel.setForeground(COLOR_TEXT_MUTED);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createDashboardPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(COLOR_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        List<Product> products = productDAO.getAllProducts();
        List<Object[]> sales = productDAO.getSalesHistory();

        int totalProducts = products.size();
        double totalSalesAmount = 0;

        Map<String, Integer> productSalesMap = new HashMap<>();
        int maxQtySold = 1;

        for (Object[] sale : sales) {
            try {
                String name = sale[1].toString();
                int qty = (Integer) sale[2];
                double amount = Double.parseDouble(sale[3].toString());
                
                totalSalesAmount += amount;
                productSalesMap.put(name, productSalesMap.getOrDefault(name, 0) + qty);
                
                if (productSalesMap.get(name) > maxQtySold) {
                    maxQtySold = productSalesMap.get(name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 1. SECTION DES CARTES (HAUT)
        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        cardsPanel.setBackground(COLOR_BG);
        
        // Plus d'émojis carrés cassés, on utilise du texte pur stylisé
        cardsPanel.add(createStatCard("TOTAL PRODUCTS", String.valueOf(totalProducts), COLOR_ACCENT));
        cardsPanel.add(createStatCard("TOTAL SALES AMOUNT", String.format("%.2f DA", totalSalesAmount), COLOR_GREEN));
        mainPanel.add(cardsPanel, BorderLayout.NORTH);

        // 2. SECTION INFERIEURE (GRAPHIQUE ET TABLEAU)
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        bottomPanel.setBackground(COLOR_BG);

        // Panneau Gauche : Meilleures ventes relooké
        JPanel leftPanel = new JPanel(new BorderLayout(15, 15));
        leftPanel.setBackground(COLOR_SURFACE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblLeftTitle = new JLabel("Top Selling Products");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLeftTitle.setForeground(COLOR_TEXT_MAIN);
        leftPanel.add(lblLeftTitle, BorderLayout.NORTH);
        
        JPanel chartContainer = new JPanel();
        chartContainer.setBackground(COLOR_SURFACE);
        chartContainer.setLayout(new BoxLayout(chartContainer, BoxLayout.Y_AXIS));

        if (productSalesMap.isEmpty()) {
            JLabel noSalesLabel = new JLabel("No sales recorded yet.", SwingConstants.CENTER);
            noSalesLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noSalesLabel.setForeground(COLOR_TEXT_MUTED);
            leftPanel.add(noSalesLabel, BorderLayout.CENTER);
        } else {
            for (Map.Entry<String, Integer> entry : productSalesMap.entrySet()) {
                JPanel rowPanel = new JPanel(new BorderLayout(15, 5));
                rowPanel.setBackground(COLOR_SURFACE);
                rowPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
                
                JLabel lblProdName = new JLabel(entry.getKey());
                lblProdName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lblProdName.setForeground(COLOR_TEXT_MAIN);
                lblProdName.setPreferredSize(new Dimension(130, 25));
                
                // Barre graphique beaucoup plus fine et élégante
                JProgressBar bar = new JProgressBar(0, maxQtySold);
                bar.setValue(entry.getValue());
                bar.setStringPainted(true);
                bar.setString(entry.getValue() + " units");
                bar.setFont(new Font("Segoe UI", Font.BOLD, 10));
                bar.setForeground(COLOR_ACCENT); // Indigo au lieu du violet flash
                bar.setBackground(COLOR_BG);
                bar.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
                bar.setPreferredSize(new Dimension(150, 18));
                
                rowPanel.add(lblProdName, BorderLayout.WEST);
                rowPanel.add(bar, BorderLayout.CENTER);
                
                chartContainer.add(rowPanel);
                chartContainer.add(Box.createVerticalStrut(12)); // Espace entre les barres
            }
            JScrollPane chartScroll = new JScrollPane(chartContainer);
            chartScroll.setBorder(null);
            chartScroll.setBackground(COLOR_SURFACE);
            leftPanel.add(chartScroll, BorderLayout.CENTER);
        }
        bottomPanel.add(leftPanel);

        // Panneau Droite : Tableau résumé relooké
        JPanel rightPanel = new JPanel(new BorderLayout(15, 15));
        rightPanel.setBackground(COLOR_SURFACE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblRightTitle = new JLabel("Products Summary");
        lblRightTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRightTitle.setForeground(COLOR_TEXT_MAIN);
        rightPanel.add(lblRightTitle, BorderLayout.NORTH);

        String[] columns = {"ID", "Product Name", "Stock Available"};
        summaryModel = new DefaultTableModel(columns, 0);
        JTable summaryTable = new JTable(summaryModel);
        styleTable(summaryTable);
        
        JScrollPane tableScroll = new JScrollPane(summaryTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        tableScroll.getViewport().setBackground(COLOR_SURFACE);
        rightPanel.add(tableScroll, BorderLayout.CENTER);
        
        bottomPanel.add(rightPanel);
        mainPanel.add(bottomPanel, BorderLayout.CENTER);

        refreshSummaryTable(products);
        return mainPanel;
    }

    // Design moderne pour les cartes de statistiques
    private JPanel createStatCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(COLOR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(COLOR_TEXT_MUTED);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(valueColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    // Méthode utilitaire pour styliser les tableaux (Look type Web)
    private void styleTable(JTable table) {
        table.setBackground(COLOR_SURFACE);
        table.setForeground(COLOR_TEXT_MAIN);
        table.setGridColor(COLOR_BORDER);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(Color.decode("#262930"));
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setShowVerticalLines(false); // Design épuré sans lignes verticales

        JTableHeader header = table.getTableHeader();
        header.setBackground(COLOR_SURFACE);
        header.setForeground(COLOR_TEXT_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
    }

    private void refreshSummaryTable(List<Product> products) {
        if (summaryModel != null) {
            summaryModel.setRowCount(0);
            for (Product p : products) {
                Object[] row = { p.getId(), p.getName(), p.getStockQuantity() };
                summaryModel.addRow(row);
            }
        }
    }

    private JPanel createStockPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(COLOR_SURFACE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = new JTextField(15);
        JTextField txtPurchasePrice = new JTextField(15);
        JTextField txtSellingPrice = new JTextField(15);
        JTextField txtStock = new JTextField(15);
        JButton btnAdd = new JButton("Add Product");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setBackground(COLOR_ACCENT);
        btnAdd.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1; formPanel.add(txtName, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Purchase Price (DA):"), gbc);
        gbc.gridx = 1; formPanel.add(txtPurchasePrice, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Selling Price (DA):"), gbc);
        gbc.gridx = 1; formPanel.add(txtSellingPrice, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1; formPanel.add(txtStock, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; formPanel.add(btnAdd, gbc);

        panel.add(formPanel, BorderLayout.WEST);

        String[] columns = {"ID", "Name", "Purchase Price", "Selling Price", "Stock"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        styleTable(table);
        
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        tableScroll.getViewport().setBackground(COLOR_SURFACE);
        panel.add(tableScroll, BorderLayout.CENTER);

        Runnable refreshTable = () -> {
            tableModel.setRowCount(0);
            List<Product> list = productDAO.getAllProducts();
            for (Product p : list) {
                Object[] row = { p.getId(), p.getName(), p.getPurchasePrice() + " DA", p.getSellingPrice() + " DA", p.getStockQuantity() };
                tableModel.addRow(row);
            }
            refreshSummaryTable(list);
        };

        refreshTable.run();

        btnAdd.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String purchaseStr = txtPurchasePrice.getText().trim();
                String sellingStr = txtSellingPrice.getText().trim();
                String stockStr = txtStock.getText().trim();

                if (name.isEmpty() || purchaseStr.isEmpty() || sellingStr.isEmpty() || stockStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields must be filled!");
                    return;
                }

                double pPrice = Double.parseDouble(purchaseStr);
                double sPrice = Double.parseDouble(sellingStr);
                int stock = Integer.parseInt(stockStr);

                Product p = new Product(name, pPrice, sPrice, stock);
                if (productDAO.addProduct(p)) {
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                    txtName.setText(""); txtPurchasePrice.setText(""); txtSellingPrice.setText(""); txtStock.setText("");
                    refreshTable.run();
                    updateAllTabs();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
            }
        });

        return panel;
    }

    private JPanel createSalePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(COLOR_SURFACE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        comboProducts = new JComboBox<>();
        List<Product> databaseProducts = productDAO.getAllProducts();
        for (Product p : databaseProducts) {
            comboProducts.addItem(p);
        }

        JTextField txtQuantity = new JTextField(10);
        JButton btnSell = new JButton("Record Sale");
        btnSell.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSell.setBackground(COLOR_GREEN);
        btnSell.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Select Product:"), gbc);
        gbc.gridx = 1; formPanel.add(comboProducts, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Quantity To Sell:"), gbc);
        gbc.gridx = 1; formPanel.add(txtQuantity, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; formPanel.add(btnSell, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COLOR_BG);
        wrapper.add(formPanel, BorderLayout.NORTH);

        panel.add(wrapper, BorderLayout.CENTER);

        btnSell.addActionListener(e -> {
            Object selectedItem = comboProducts.getSelectedItem();
            String qtyStr = txtQuantity.getText().trim();

            if (selectedItem == null || qtyStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!");
                return;
            }

            Product selected = (Product) selectedItem;

            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!");
                    return;
                }
                if (qty > selected.getStockQuantity()) {
                    JOptionPane.showMessageDialog(this, "Insufficient stock! Only " + selected.getStockQuantity() + " available.");
                    return;
                }

                int newStock = selected.getStockQuantity() - qty;
                boolean stockUpdated = productDAO.updateProductStock(selected.getId(), newStock);
                
                double total = qty * selected.getSellingPrice();
                boolean saleSaved = productDAO.saveSale(selected.getId(), selected.getName(), qty, total);

                if (stockUpdated && saleSaved) {
                    JOptionPane.showMessageDialog(this, "Sale recorded successfully!");
                    txtQuantity.setText("");
                    updateAllTabs();
                } else {
                    JOptionPane.showMessageDialog(this, "Database update failed.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid round number.");
            }
        });

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Sale ID", "Product Name", "Quantity", "Total Amount", "Date"};
        historyModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(historyModel);
        styleTable(table);

        List<Object[]> salesHistory = productDAO.getSalesHistory();
        for (Object[] row : salesHistory) {
            Object[] formattedRow = { row[0], row[1], row[2], row[3] + " DA", row[4] };
            historyModel.addRow(formattedRow);
        }

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        tableScroll.getViewport().setBackground(COLOR_SURFACE);
        panel.add(tableScroll, BorderLayout.CENTER);
        return panel;
    }

    // Force le rafraîchissement propre de l'UI globale après une action
    private void updateAllTabs() {
        tabbedPane.setComponentAt(0, createDashboardPanel());
        tabbedPane.setComponentAt(1, createStockPanel());
        tabbedPane.setComponentAt(2, createSalePanel());
        tabbedPane.setComponentAt(3, createHistoryPanel());
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    public static void launch() {
        try {
            FlatDarkLaf.setup();
            
            // Surcharger globalement quelques propriétés FlatLaf importantes
            UIManager.put("TabbedPane.selectedBackground", Color.decode("#1A1C1E")); 
            UIManager.put("TabbedPane.underlineColor", Color.decode("#6366F1"));
            UIManager.put("TabbedPane.selectedForeground", Color.decode("#6366F1"));  
            UIManager.put("Button.arc", 8); // Boutons légèrement arrondis modernes
            UIManager.put("Component.arc", 8);
            
        } catch (Exception ex) {
            System.err.println("Theme error: " + ex.getMessage());
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}