package com.service;

import com.intuit.ipp.data.Entity;
import com.intuit.ipp.data.SalesTransaction;
import com.intuit.ipp.exception.FMSException;

import java.util.List;

public interface BaseService<T extends SalesTransaction> extends Persistable<T> {

    List<T> calculate(List<SalesTransaction> entities) throws FMSException;

    void calculate();

    void process(Entity entity) throws FMSException;

    String getEntityName();
}

