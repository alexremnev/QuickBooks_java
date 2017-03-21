package com.service;

import com.dao.ReportDAO;
import com.dao.TaxRateDAO;
import com.intuit.ipp.data.*;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.DataService;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings("unchecked")
@Service
public class InvoiceServiceImpl extends BaseServiceImpl<Invoice> implements InvoiceService {
    public InvoiceServiceImpl(OauthService oauthService, ReportDAO reportDAO, TaxRateDAO taxRateDAO) {
        super(oauthService, reportDAO, taxRateDAO);
    }

    @Override
    public String getEntityName() {
        return "Invoice";
    }

    @Override
    public void save(List<SalesTransaction> list) throws FMSException {
        if (getAccountingMethod() == ReportBasisEnum.ACCRUAL) {
            super.save(list);
            return;
        }
        List<Invoice> entities = oauthService.getDataService().findAll(new Invoice());
        List<SalesTransaction> paidedInvoices = (List<SalesTransaction>) entities.stream().filter(salesTransaction -> salesTransaction.getLinkedTxn() != null && isPaid(salesTransaction));
        super.save(paidedInvoices);
    }

    private static Boolean isPaid(Transaction invoice) {
        List<LinkedTxn> lines = invoice.getLinkedTxn();
        return lines.stream().allMatch(linkedTxn -> linkedTxn.getTxnType().compareTo("Payment") == 0 || (linkedTxn.getTxnType().compareTo("ReimburseCharge")) == 0);
    }

    private ReportBasisEnum getAccountingMethod() throws FMSException {
        DataService dataService = oauthService.getDataService();
        List<Preferences> preferences = dataService.findAll(new Preferences());
        return preferences.get(0).getReportPrefs().getReportBasis();

    }
}
