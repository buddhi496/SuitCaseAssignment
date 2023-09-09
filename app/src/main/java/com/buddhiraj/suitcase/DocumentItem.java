package com.buddhiraj.suitcase;

public class DocumentItem {
    private final byte[] image;
    private final String name;
    private final String price;
    private final String description;
    private final String storeName;

    public DocumentItem(byte[] image, String name, String price, String description, String storeName) {
        this.image = image;
        this.name = name;
        this.price = price;
        this.description = description;
        this.storeName = storeName;
    }

    public byte[] getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getStoreName() {
        return storeName;
    }
}
