package com.pc.logistics.model;

/**
 * TODO
 *
 * @author pengchao
 * @date 22:28 2020-07-02
 */
public class Order {

    private Long id;
    private String name;
    private String code;
    private Double price;


    public Order(Long id, String name, String code, Double price) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }


    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", price=" + price +
                '}';
    }
}
