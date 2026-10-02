package dao;

import database.DatabaseConnection;
import model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
   
    // 1. ADD A PRODUCT
    public boolean addProduct(Product product) {
        String query = "INSERT INTO products (name, purchase_price, selling_price, stock_quantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPurchasePrice());
            pstmt.setDouble(3, product.getSellingPrice());
            pstmt.setInt(4, product.getStockQuantity());
            
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 2. GET ALL PRODUCTS
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String query = "SELECT id, name, purchase_price, selling_price, stock_quantity FROM products";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Product p = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("purchase_price"),
                    rs.getDouble("selling_price"),
                    rs.getInt("stock_quantity")
                );
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. UPDATE PRODUCT STOCK
    public boolean updateProductStock(int id, int quantity) {
        String query = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4. SAVE A NEW SALE (Writes to both 'sales' and 'sale_items')
    public boolean saveSale(int productId, String productName, int quantity, double totalAmount) {
        String insertSaleQuery = "INSERT INTO sales (total_amount) VALUES (?)";
        String insertItemQuery = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start database transaction

            int saleId = -1;
            // Step A: Insert into sales table to generate a new sale ID
            try (PreparedStatement pstmtSale = conn.prepareStatement(insertSaleQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmtSale.setDouble(1, totalAmount);
                pstmtSale.executeUpdate();
                
                try (ResultSet rs = pstmtSale.getGeneratedKeys()) {
                    if (rs.next()) {
                        saleId = rs.getInt(1);
                    }
                }
            }

            // Step B: Insert the transaction item into sale_items table using the generated sale ID
            if (saleId != -1) {
                try (PreparedStatement pstmtItem = conn.prepareStatement(insertItemQuery)) {
                    pstmtItem.setInt(1, saleId);
                    pstmtItem.setInt(2, productId);
                    pstmtItem.setInt(3, quantity);
                    pstmtItem.setDouble(4, totalAmount / quantity); // Calculates item unit price
                    pstmtItem.executeUpdate();
                }
                
                conn.commit(); // Finalize database write
                return true;
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Database Error: " + e.getMessage(), 
                "SQL Exception", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return false;
    }

    // 5. GET SALES HISTORY (Combines tables with an SQL JOIN)
    public List<Object[]> getSalesHistory() {
        List<Object[]> history = new ArrayList<>();
        String query = "SELECT s.id, p.name AS product_name, si.quantity, s.total_amount, s.sale_date " +
                       "FROM sales s " +
                       "JOIN sale_items si ON s.id = si.sale_id " +
                       "JOIN products p ON si.product_id = p.id " +
                       "ORDER BY s.sale_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("total_amount"), 
                    rs.getTimestamp("sale_date").toString()
                };
                history.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}