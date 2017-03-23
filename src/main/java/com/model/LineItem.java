package com.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "lineitem")
public class LineItem {

    @Id
    @GenericGenerator(name = "gen", strategy = "increment")
    @GeneratedValue(generator = "gen")
    private int Id;
    private String Name;
    private BigDecimal Quantity;
    private BigDecimal Amount;

    public LineItem() {
    }

    public void setName(String name) {
        Name = name;
    }

    public void setQuantity(BigDecimal quantity) {
        Quantity = quantity;
    }

    public BigDecimal getAmount() {
        return Amount;
    }

    public void setAmount(BigDecimal amount) {
        Amount = amount;
    }
}


