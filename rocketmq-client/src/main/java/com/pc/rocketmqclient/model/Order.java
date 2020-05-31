package com.pc.rocketmqclient.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private Long orderId;
    private String type;

    private LocalDateTime localDateTime;


    public Order(Long orderId, String type,LocalDateTime localDateTime) {
        this.orderId = orderId;
        this.type = type;
        this.localDateTime = localDateTime;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public static List<Order> getOrderList() {
        List<Order> orderList = new ArrayList<>();
        orderList.add(new Order(111L, "创建订单",LocalDateTime.now()));
        orderList.add(new Order(222L, "创建订单",LocalDateTime.now()));
        orderList.add(new Order(333L, "创建订单",LocalDateTime.now()));

        orderList.add(new Order(111L, "支付成功",LocalDateTime.now()));
        orderList.add(new Order(222L, "支付成功",LocalDateTime.now()));
        orderList.add(new Order(333L, "支付成功",LocalDateTime.now()));

        orderList.add(new Order(111L, "完成订单",LocalDateTime.now()));
        orderList.add(new Order(222L, "完成订单",LocalDateTime.now()));
        orderList.add(new Order(333L, "完成订单",LocalDateTime.now()));


        return orderList;
    }


    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", type='" + type + '\'' +
                ", localDateTime=" + localDateTime +
                '}';
    }
}
