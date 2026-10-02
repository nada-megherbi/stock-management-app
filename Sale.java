package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private Timestamp saleDate;
    private List<SaleItem> items; // Une vente contient plusieurs lignes de produits

    public Sale() {
        this.items = new ArrayList<>();
    }

    public Sale(int id, Timestamp saleDate) {
        this.id = id;
        this.saleDate = saleDate;
        this.items = new ArrayList<>();
    }

    // Méthode pour calculer le montant total de la vente
    public double getTotalAmount() {
        double total = 0;
        for (SaleItem item : items) {
            total += item.getQuantity() * item.getUnitPrice();
        }
        return total;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Timestamp getSaleDate() { return saleDate; }
    public void setSaleDate(Timestamp saleDate) { this.saleDate = saleDate; }

    public List<SaleItem> getItems() { return items; }
    public void addItem(SaleItem item) { this.items.add(item); }
}