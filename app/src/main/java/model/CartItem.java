package model;

public class CartItem {

    private String id;

    private int pid;

    private String name;

    private int qty;

    private double price;

    private  String img_url;

    public CartItem(String name, String id,int pid, int qty, double price, String img_url) {
        this.name = name;
        this.id = id;
        this.pid = pid;
        this.qty = qty;
        this.price = price;
        this.img_url = img_url;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public CartItem() {

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

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }



}
