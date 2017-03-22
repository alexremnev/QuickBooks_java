
package com.dao;

import com.model.TaxRate;

import java.util.List;

/**
 * Represents base operations for {@code TaxRate} model.
 */
public interface TaxRateDAO {
    /**
     * Gets {@code TaxRate} entity by appropriate state.
     *
     * @param state entity state.
     * @return the entity of tax rate.
     */
    TaxRate getByCountrySubDivisionCode(String state);

    /**
     * Returns list of {@code TaxRate}'s.
     *
     * @return list of tax rates.
     */
    List<TaxRate> list();
}
