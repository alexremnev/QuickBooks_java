package com.service;

import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.SalesTransaction;
import com.intuit.ipp.exception.FMSException;

import java.util.List;

public interface InvoiceService extends BaseService<Invoice> {
    void save(List<SalesTransaction> list) throws FMSException;
}
