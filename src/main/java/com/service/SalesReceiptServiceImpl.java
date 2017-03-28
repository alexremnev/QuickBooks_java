package com.service;

import com.dao.ReportDAO;
import com.dao.TaxRateDAO;
import com.intuit.ipp.data.*;
import com.intuit.ipp.exception.FMSException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalesReceiptServiceImpl extends BaseServiceImpl<SalesReceipt> implements SalesReceiptService {
    public SalesReceiptServiceImpl(OauthService oauthService, ReportDAO reportDAO, TaxRateDAO taxRateDAO) {
        super(oauthService, reportDAO, taxRateDAO);
    }

    @Override
    public String getEntityName() {
        return "SalesReceipt";
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SalesReceipt> calculate(List<SalesTransaction> list) throws FMSException {
        deleteDepositedSalesReceipts(list);
        return super.calculate(list);
    }

    private void deleteDepositedSalesReceipts(List<SalesTransaction> recalculateEntity) throws FMSException {
        if (recalculateEntity == null) recalculateEntity = getAllSalesReceipts();
        List<Deposit> deposits = getAllDeposits();
        for (SalesTransaction salesReceipt : recalculateEntity) {
            Deposit deposit = findDeposit(deposits, salesReceipt);
            if (deposit != null) oauthService.getDataService().delete(deposit);
        }
    }

    private List<Deposit> getAllDeposits() throws FMSException {
        return oauthService.getDataService().findAll(new Deposit());
    }

    private List<SalesTransaction> getAllSalesReceipts() throws FMSException {
        return oauthService.getDataService().findAll(new SalesReceipt());
    }

    private static Deposit findDeposit(List<Deposit> deposits, IntuitEntity salesReceipt) {
        for (Deposit deposit : deposits) {
            if (deposit.getLine() != null) {
                if (deposit.getLinkedTxn() != null) {
                    for (LinkedTxn linkedTxns : deposit.getLinkedTxn()) {
                        if (linkedTxns.getTxnId().compareTo(salesReceipt.getId()) == 0) {
                            return deposit;
                        }
                    }
                }
            }
        }
        return null;
    }
}

