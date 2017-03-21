package com.service;


import com.intuit.ipp.data.CreditMemo;
import com.intuit.ipp.data.SalesTransaction;
import com.intuit.ipp.exception.FMSException;

import java.util.List;

public interface CreditMemoService extends BaseService<CreditMemo> {
    void save(List<SalesTransaction> entities) throws FMSException;
}
