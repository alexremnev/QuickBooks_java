package com.service;

import com.intuit.ipp.data.SalesTransaction;

/**
 * Represents base operations for calculating and processing {@code com.intuit.ipp.data.SalesTransaction}'s.
 *
 * @param <T> the type of calculated entity.
 */
public interface BaseService<T extends SalesTransaction> extends Persistable<T> {

}

