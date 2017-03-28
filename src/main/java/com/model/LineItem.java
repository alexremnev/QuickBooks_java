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
    private int Hash;

    public LineItem() {
    }

    public void setName(String name) {
        Name = name;
    }

    public void setQuantity(BigDecimal quantity) {
        Quantity = quantity;
    }

    public void setAmount(BigDecimal amount) {
        Amount = amount;
    }

    public void setHash(int hash) {
        Hash = hash;
    }

    public int getHash() {
        return Hash;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((Name == null) ? 0 : Name.hashCode());
        result = prime * result + Id;
        result = (prime * result) + ((Quantity == null) ? 0 : Quantity.hashCode());
        result = (prime * result) + ((Amount == null) ? 0 : Amount.hashCode());
        return result;
    }
}


