package com.service;

import com.intuit.ipp.data.SalesReceipt;
import com.intuit.ipp.data.SalesTransaction;
import com.intuit.ipp.exception.FMSException;

import java.util.List;

/**
 * Represents operations for {@code @code com.intuit.ipp.data.SalesReceipt}.
 */
public interface SalesReceiptService extends BaseService<SalesReceipt> {
    /**
     * Save the list of SalesReceipt in database.
     *
     * @param entities the list of {@code @code com.intuit.ipp.data.SalesReceipt}'s.
     */
    void save(List<SalesTransaction> entities) throws FMSException;
}
