package com.service;

import com.intuit.ipp.data.Entity;
import com.intuit.ipp.data.SalesTransaction;
import com.intuit.ipp.exception.FMSException;

import java.util.List;

public interface ProcessService {
    void process(Entity entity) throws FMSException;

    void calculate();

    String getEntityName();
}
