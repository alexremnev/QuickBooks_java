package com.service;

import com.dao.ReportDAO;
import com.dao.TaxRateDAO;
import com.intuit.ipp.data.*;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.query.GenerateQuery;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.GlobalTaxService;
import com.intuit.ipp.services.QueryResult;
import com.model.LineItem;
import com.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;

import java.lang.Class;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intuit.ipp.query.GenerateQuery.$;
import static com.intuit.ipp.query.GenerateQuery.select;

@SuppressWarnings("unchecked")
@Service
public abstract class BaseServiceImpl<T extends SalesTransaction> implements BaseService<T> {
    private Logger logger;
    private Class<T> entityClass;
    OauthService oauthService;
    private ReportDAO reportDAO;
    private TaxRateDAO taxRateDAO;
    private Map<String, BigDecimal> taxRateMap;
    private String entityName;

    @Autowired
    public BaseServiceImpl(OauthService oauthService, ReportDAO reportDAO, TaxRateDAO taxRateDAO) {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
        logger = LoggerFactory.getLogger(this.getClass());
        this.oauthService = oauthService;
        this.reportDAO = reportDAO;
        this.taxRateDAO = taxRateDAO;
        this.entityName = getEntityName();
    }

    public String getEntityName() {
        return entityName;
    }

    public void calculate() {
        try {
            List<SalesTransaction> entities = oauthService.getDataService().findAll(entityClass.newInstance());
            calculate(entities);
        } catch (Exception e) {
            logger.error("Exception occured when application tried to recalculate sales tax in " + entityName, e.getCause());
        }
    }

    public List calculate(List<SalesTransaction> entities) throws FMSException {
        try {
            taxRateMap = getCustomerTaxRate();
            DataService dataService = oauthService.getDataService();
            if (entities.size() == 0) return new ArrayList<>();
            for (SalesTransaction entity : entities) {
                setTaxCode(entity);
                if ((entity.getShipAddr() == null) || (entity.getShipAddr().getCountrySubDivisionCode() == null))
                    continue;
                String countrySubDivisionCode = entity.getShipAddr().getCountrySubDivisionCode();
                BigDecimal percent = getPercent(countrySubDivisionCode);
                setTaxCodeRef(percent, entity);
                if (entity.getTxnTaxDetail().getTotalTax().compareTo(new BigDecimal(0)) == 0) {
                    recalculateTaxManually(entity, percent);
                }
                dataService.update(entity);
            }
            return entities;
        } catch (Exception e) {
            logger.error("Exception occured when application tried to recalculate sales tax in " + entityName, e.getCause());
            throw e;
        }
    }

    public void save() throws FMSException {
        try {
            List<SalesTransaction> entities = oauthService.getDataService().findAll(entityClass.newInstance());
            save(entities);

        } catch (Exception e) {
            logger.error("Exception occured when application tried to save " + entityName, e.getCause());
        }
    }

    public void save(List<SalesTransaction> entities) throws FMSException {
        for (SalesTransaction entity : entities) {
            Report report = new Report();
            List<LineItem> lineItemList = new ArrayList<>();
            report.setId(entity.getId());
            report.setSaleDate(entity.getTxnDate());
            report.setDocumentNumber(entity.getDocNumber());
            if (entity.getBillAddr().getLine1() != null) report.setCustomerName(entity.getCustomerRef().getName());
            StringBuilder address = new StringBuilder();
            if (entity.getShipAddr() != null) {
                if (entity.getShipAddr().getLine1() != null)
                    address.append(entity.getShipAddr().getLine1()).append(" ");
                if ((entity.getShipAddr().getCity()) != null)
                    address.append(entity.getShipAddr().getCity()).append(" ");
                if (entity.getShipAddr().getCountrySubDivisionCode() != null)
                    address.append(entity.getShipAddr().getCountrySubDivisionCode()).append(" ");
                if (entity.getShipAddr().getPostalCode() != null)
                    address.append(entity.getShipAddr().getPostalCode());
            }
            report.setShipToAddress(address.toString());
            if (entity.getLine() == null) {
                reportDAO.save(report);
                continue;
            }
            for (Line line : entity.getLine()) {
                LineItem lineItem = new LineItem();
                if (line == null) continue;
                lineItem.setAmount(line.getAmount());
                if (line.getSalesItemLineDetail() != null) {
                    lineItem.setQuantity(line.getSalesItemLineDetail().getQty());
                    if ((line.getSalesItemLineDetail().getItemRef() != null) && (line.getSalesItemLineDetail().getItemRef().getName() != null))
                        lineItem.setName(line.getSalesItemLineDetail().getItemRef().getName());
                }
                lineItemList.add(lineItem);
            }
            report.setLineItems(lineItemList);
            reportDAO.save(report);
        }
    }


    private Map<String, BigDecimal> getCustomerTaxRate() {
        List<com.model.TaxRate> taxRates = taxRateDAO.list();
        taxRateMap = new HashMap<>();
        for (com.model.TaxRate taxRate : taxRates)
            taxRateMap.put(taxRate.getCountrySubDivisionCode(), taxRate.getTax());
        return taxRateMap;
    }

    private void setTaxCode(SalesTransaction entity) {
        Boolean isNeedToAddLine = true;
        for (Line line : entity.getLine()) {
            if (line.getSalesItemLineDetail() == null) continue;
            SalesItemLineDetail lineDetail = line.getSalesItemLineDetail();
            if (isNeedToAddLine) isNeedToAddLine = false;
            if (lineDetail.getTaxCodeRef() == null) lineDetail.getTaxCodeRef().setValue("TAX");
            lineDetail.getTaxCodeRef().setValue("Tax");
        }
        if (isNeedToAddLine) addLine(entity);
    }

    private static void addLine(Transaction entity) {
        for (Line line : entity.getLine()) {
            SubTotalLineDetail subTotalLineItem = line.getSubTotalLineDetail();
            if (subTotalLineItem == null) continue;
            Line newLine = new Line();
            newLine.setAmount(line.getAmount());
            newLine.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
            newLine.setLineNum(new BigInteger("1"));
            SalesItemLineDetail salesItemLineDetail = new SalesItemLineDetail();
            ReferenceType referenceForItem = new ReferenceType();
            referenceForItem.setValue("1");
            ReferenceType referenceForTax = new ReferenceType();
            referenceForTax.setValue("TAX");
            salesItemLineDetail.setItemRef(referenceForItem);
            salesItemLineDetail.setTaxCodeRef(referenceForTax);
            newLine.setSalesItemLineDetail(salesItemLineDetail);
            List<Line> lines = new ArrayList<>();
            lines.add(newLine);
            lines.add(line);
            entity.setLine(lines);
            return;
        }
    }

    private BigDecimal getPercent(String countrySubDivisionCode) {
        BigDecimal taxRate;
        if ((countrySubDivisionCode == null) || (countrySubDivisionCode.isEmpty())) return taxRateMap.get("DEFAULT");
        if (taxRateMap.containsKey(countrySubDivisionCode.toUpperCase())) {
            taxRate = taxRateMap.get(countrySubDivisionCode.toUpperCase());
            return taxRate;
        }
        taxRate = taxRateMap.get("DEFAULT");
        return taxRate;
    }

    private void setTaxCodeRef(BigDecimal percent, Transaction entity) throws FMSException {
        String taxRateRef = getTxnCodeRefValue(percent);
        ReferenceType referenceType = new ReferenceType();
        referenceType.setValue(taxRateRef);
        entity.getTxnTaxDetail().setTxnTaxCodeRef(referenceType);
    }

    @SuppressWarnings("unchecked")
    private String getTxnCodeRefValue(BigDecimal taxRate) throws FMSException {
        String taxCodeId = getTaxRateId(taxRate);
        if (taxCodeId != null) {
            DataService service = oauthService.getDataService();
            String query = "SELECT * FROM TaxCode";
            QueryResult queryResult = service.executeQuery(query);
            List<TaxCode> stateTaxCodes = (List<TaxCode>) queryResult.getEntities();
            TaxCode taxCode = null;
            for (TaxCode code : stateTaxCodes) {
                if ((code != null) && (code.getSalesTaxRateList() != null) && (code.getSalesTaxRateList().getTaxRateDetail() != null) &&
                        (code.getSalesTaxRateList().getTaxRateDetail().get(0) != null) && (code.getSalesTaxRateList().getTaxRateDetail().get(0).getTaxRateRef() != null) &&
                        (code.getSalesTaxRateList().getTaxRateDetail().get(0).getTaxRateRef().getValue() != null) &&
                        ((code.getSalesTaxRateList().getTaxRateDetail().get(0).getTaxRateRef().getValue().compareTo(taxCodeId) == 0))) {
                    taxCode = code;
                    break;
                }
            }
            if (taxCode != null) {
                return taxCode.getId();
            }
        }
        TaxService taxService = addTaxService(taxRate);
        return taxService.getTaxRateDetails().get(0).getTaxRateId();
    }

    private TaxService addTaxService(BigDecimal percent) throws FMSException {
        TaxAgency taxAgency = new TaxAgency();
        DataService service = oauthService.getDataService();
        TaxAgency taxagency = GenerateQuery.createQueryEntity(TaxAgency.class);
        String query = select($(taxagency)).generate();
        query = query.replaceAll("tring.", "");
        //String query = "Select * From TaxAgency";
        QueryResult queryResult = service.executeQuery(query);
        if (queryResult != null) taxAgency = (TaxAgency) queryResult.getEntities().get(0);
        if (taxAgency == null) {
            TaxAgency agency = new TaxAgency();
            agency.setDisplayName("New Tax Agency");
            taxAgency = service.add(agency);
        }
        String name = percent + " percent tax";
        TaxRateDetails taxRateDetails = new TaxRateDetails();
        taxRateDetails.setRateValue(percent);
        taxRateDetails.setTaxAgencyId(taxAgency.getId());
        taxRateDetails.setTaxApplicableOn(TaxRateApplicableOnEnum.SALES);
        taxRateDetails.setTaxRateName(name);
        List<TaxRateDetails> rateDetailsList = new ArrayList<>();
        rateDetailsList.add(taxRateDetails);
        TaxService taxService = new TaxService();
        taxService.setTaxCode(name);
        taxService.setTaxRateDetails(rateDetailsList);
        GlobalTaxService globalTaxService = new GlobalTaxService(oauthService.getContext());
        taxService = globalTaxService.addTaxCode(taxService);
        return taxService;
    }

    private String getTaxRateId(BigDecimal taxRate) throws FMSException {
        List<com.intuit.ipp.data.TaxRate> taxRates = oauthService.getDataService().findAll(new com.intuit.ipp.data.TaxRate());
        for (com.intuit.ipp.data.TaxRate rateValue : taxRates) {
            if ((rateValue.getRateValue().compareTo(taxRate) == 0) && (rateValue.isActive())) {
                return rateValue.getId();
            }
        }
        return null;
    }

    private void recalculateTaxManually(SalesTransaction entity, BigDecimal percent) {
        BigDecimal totalTax = BigDecimal.valueOf(0);
        for (Line line : entity.getLine()) {
            SalesItemLineDetail lineDetail = line.getSalesItemLineDetail();
            if (lineDetail == null) continue;
            totalTax = totalTax.add(line.getAmount().multiply(percent.divide(new BigDecimal("100"), BigDecimal.ROUND_CEILING)));
        }
        entity.getTxnTaxDetail().setTotalTax(totalTax);
    }

    @SuppressWarnings("unchecked")
    public void process(Entity entity) throws FMSException {
        if (entity.getOperation().equals("Delete")) {
            reportDAO.remove(entity.getId());
            return;
        }
        DataService dataService = oauthService.getDataService();
        Invoice invoice = GenerateQuery.createQueryEntity(Invoice.class);
        String query = select($(invoice)).where($(invoice.getId()).eq(entity.getId())).generate();
        query = query.replaceAll("tring.", "");
        QueryResult result = dataService.executeQuery(query);
        List<SalesTransaction> entityFromQuickBooks = (List<SalesTransaction>) result.getEntities();
        if (entityFromQuickBooks.size() == 0) return;
        if (!entity.getOperation().equals("Create")) {
            if (!entity.getOperation().equals("Update")) return;
            Report reportEntity = reportDAO.get(entity.getId());
            if (reportEntity == null) {
                calculate(entityFromQuickBooks);
                return;
            }
            if (isEqualLines(entityFromQuickBooks.get(0).getLine(), reportEntity.getLineItems())) return;
            List<SalesTransaction> recalculatedList = calculate(entityFromQuickBooks);
            reportDAO.remove(entity.getId());
            save(recalculatedList);
        } else {
            List<SalesTransaction> recalculatedList = calculate(entityFromQuickBooks);
            save(recalculatedList);
        }
    }

    private static Boolean isEqualLines(List<Line> quickBookslines, List<LineItem> actualLines) {
        if (quickBookslines.size() - actualLines.size() != 0) return false;
        for (int i = 0; i < quickBookslines.size(); i++) {
            if (quickBookslines.get(i).getDetailType() == LineDetailTypeEnum.SUB_TOTAL_LINE_DETAIL) return false;
            if (quickBookslines.get(i).getAmount().compareTo(actualLines.get(i).getAmount()) == 0) return false;
        }
        return true;
    }
}
