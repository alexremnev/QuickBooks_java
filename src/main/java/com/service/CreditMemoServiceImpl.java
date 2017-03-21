package com.service;

import com.dao.ReportDAO;
import com.dao.TaxRateDAO;
import com.intuit.ipp.data.CreditMemo;
import org.springframework.stereotype.Service;

@Service
public class CreditMemoServiceImpl extends BaseServiceImpl<CreditMemo> implements CreditMemoService {

    public CreditMemoServiceImpl(OauthService oauthService, ReportDAO reportDAO, TaxRateDAO taxRateDAO) {
        super(oauthService, reportDAO, taxRateDAO);
    }

    @Override
    public String getEntityName() {
        return "SalesReceiptService";
    }
}
