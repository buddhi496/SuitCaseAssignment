package com.buddhiraj.suitcase;

public class Item {
    private String itemName;
    private String itemDescription;
    private double itemPrice;
    private String storeName;
    private String imageUrl; // If you want to store an image URL

    // Constructors, getters, setters

    // Default constructor
    public Item() {
        // Empty constructor needed for Firebase
    }

    // Parameterized constructor
    public Item(String itemName, String itemDescription, double itemPrice, String storeName, String imageUrl) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemPrice = itemPrice;
        this.storeName = storeName;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

