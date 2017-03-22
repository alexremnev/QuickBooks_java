package com.service;

import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.SalesTransaction;
import com.intuit.ipp.exception.FMSException;

import java.util.List;

/**
 * Represents operations for {@code @code com.intuit.ipp.data.Invoice}.
 */
public interface InvoiceService extends BaseService<Invoice> {
    /**
     * Save the list of Invoice in database.
     *
     * @param entities the list of {@code @code com.intuit.ipp.data.Invoice}'s.
     */
    void save(List<SalesTransaction> entities) throws FMSException;
}
