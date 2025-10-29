package model;

public class FoodItem {

    private String id;
    private String name;
    private double fullPrice;
    private double normalPrice;
    private String qty;
    private String description;

    private String itemImgUrl;

    public FoodItem(String id, String name, double normalPrice,double fullPrice, String qty, String description, String itemImgUrl) {
        this.id = id;
        this.name = name;
        this.normalPrice = normalPrice;
        this.fullPrice = fullPrice;
        this.qty = qty;
        this.description = description;
        this.itemImgUrl = itemImgUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(double fullPrice) {
        this.fullPrice = fullPrice;
    }

    public double getNormalPrice() {
        return normalPrice;
    }

    public void setNormalPrice(double normalPrice) {
        this.normalPrice = normalPrice;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemImgUrl() {
        return itemImgUrl;
    }

    public void setItemImgUrl(String itemImgUrl) {
        this.itemImgUrl = itemImgUrl;
    }
}
