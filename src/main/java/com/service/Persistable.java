package com.service;

import com.intuit.ipp.data.SalesTransaction;
import com.intuit.ipp.exception.FMSException;

import java.util.List;

/**
 * Represent save operations for {@code com.intuit.ipp.data.SalesTransaction}'s entities.
 *
 * @param <T> the type of persistable entity.
 */
public interface Persistable<T extends SalesTransaction> extends Calculable {
    /**
     * Saves list of {@code com.intuit.ipp.data.SalesTransaction}'s entities.
     *
     * @param entities list of {@code com.intuit.ipp.data.SalesTransaction}'s.
     */
    void save(List<SalesTransaction> entities) throws FMSException;

    /**
     * Saves all {@code com.intuit.ipp.data.SalesTransaction}'s.
     */
    void save() throws FMSException;
}
