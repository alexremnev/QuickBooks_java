package com.service;

import com.dao.ReportDAO;
import com.dao.TaxRateDAO;
import com.intuit.ipp.data.Estimate;
import com.intuit.ipp.data.SalesTransaction;
import com.intuit.ipp.exception.FMSException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstimateServiceImpl extends BaseServiceImpl<Estimate> implements EstimateService {

    public EstimateServiceImpl(OauthService oauthService, ReportDAO reportDAO, TaxRateDAO taxRateDAO) {
        super(oauthService, reportDAO, taxRateDAO);
    }

    @Override
    public void save(List<SalesTransaction> entities) throws FMSException {
    }

    @Override
    public String getEntityName() {
        return "Estimate";
    }
}
