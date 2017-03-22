package com.service;


import com.intuit.ipp.data.CreditMemo;
import com.intuit.ipp.data.SalesTransaction;
import com.intuit.ipp.exception.FMSException;

import java.util.List;

/**
 * Represents operations for {@code @code com.intuit.ipp.data.CreditMemo}.
 */
public interface CreditMemoService extends BaseService<CreditMemo> {
    /**
     * Save the list of Credit Memo in database.
     *
     * @param entities the list of {@code @code com.intuit.ipp.data.CreditMemo}'s.
     */
    void save(List<SalesTransaction> entities) throws FMSException;
}
