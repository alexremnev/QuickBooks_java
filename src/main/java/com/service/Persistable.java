package com.service;

import com.intuit.ipp.data.SalesTransaction;
import com.intuit.ipp.exception.FMSException;

import java.util.List;

public interface Persistable<T extends SalesTransaction> extends ProcessService {
    void save(List<SalesTransaction> entities) throws FMSException;
    void save() throws FMSException;

}
