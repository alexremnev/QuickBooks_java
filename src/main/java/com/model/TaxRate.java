package com.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "taxrate")
public class TaxRate {
    @Id
    private int Id;
    private String CountrySubDivisionCode;
    private BigDecimal Tax;

    public String getCountrySubDivisionCode() {
        return CountrySubDivisionCode;
    }

    public BigDecimal getTax() {
        return Tax;
    }
}
