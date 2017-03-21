package com.dao;

import com.model.TaxRate;

import java.util.List;

public interface TaxRateDAO {
    TaxRate getByCountrySubDivisionCode(String state);
    List<TaxRate> list();
}
