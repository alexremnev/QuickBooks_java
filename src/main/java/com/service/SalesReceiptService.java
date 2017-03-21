package com.service;

import com.intuit.ipp.data.SalesReceipt;
import com.intuit.ipp.data.SalesTransaction;
import com.intuit.ipp.exception.FMSException;

import java.util.List;

public interface SalesReceiptService extends BaseService<SalesReceipt> {
    void save(List<SalesTransaction> list) throws FMSException;;
}
