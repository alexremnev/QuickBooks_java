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
    private static Logger logger;
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
            List<SalesTransaction> entities = getAllEnitities();
            calculate(entities);
        } catch (Exception e) {
            logger.error("Exception occured when application recalculated sales tax", e);
        }
    }

    protected List calculate(List<SalesTransaction> entities) throws FMSException {
        try {
            if (entities.size() == 0) return new ArrayList<>();
            taxRateMap = getCustomerTaxRate();
            for (SalesTransaction entity : entities) {
                if ((entity.getShipAddr() == null) || (entity.getShipAddr().getCountrySubDivisionCode() == null))
                    continue;
                recalculateDocument(entity);
                updateDocument(entity);
            }
            return entities;
        } catch (Exception e) {
            logger.error("Exception occured when application recalculated all sales tax", e);
            throw e;
        }
    }

    public void save() throws FMSException {
        try {
            List<SalesTransaction> entities = getAllEnitities();
            save(entities);

        } catch (Exception e) {
            logger.error("Exception occured when application saved entity", e);
        }
    }

    public void save(List<SalesTransaction> entities) throws FMSException {
        for (SalesTransaction entity : entities) {
            Report report = composeReport(entity);
            if (report == null) continue;
            reportDAO.save(report);
        }
    }

    @SuppressWarnings("unchecked")
    public void process(Entity entity) {
        try {
            if (entity.getOperation().equals("Delete")) {
                reportDAO.delete(entity.getId());
                return;
            }
            List<SalesTransaction> entityFromQuickBooks = getEntityFromQuickBooks(entity);
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
                reportDAO.delete(entity.getId());
                save(recalculatedList);
            } else {
                List<SalesTransaction> recalculatedList = calculate(entityFromQuickBooks);
                save(recalculatedList);
            }
        } catch (FMSException | InstantiationException | IllegalAccessException e) {
            logger.error("Exception occured when application tried to prosess incoming entities", e);
        }
    }

    private List<SalesTransaction> getAllEnitities() throws InstantiationException, IllegalAccessException, FMSException {
        try {
            DataService dataService = getDataService();
            return dataService.findAll(entityClass.newInstance());
        } catch (FMSException e) {
            logger.error("Exception occured when application got list of entities from QuickBooks", e);
            throw e;
        }
    }

    private void recalculateDocument(SalesTransaction entity) throws FMSException {
        setTaxCode(entity);
        String countrySubDivisionCode = entity.getShipAddr().getCountrySubDivisionCode();
        BigDecimal percent = getPercent(countrySubDivisionCode);
        setTaxCodeRef(percent, entity);
        if (entity.getTxnTaxDetail().getTotalTax().compareTo(new BigDecimal(0)) == 0) {
            recalculateTaxManually(entity, percent);
        }
    }

    private Report composeReport(SalesTransaction entity) {
        Report report = new Report();
        report.setId(entity.getId());
        report.setSaleDate(entity.getTxnDate());
        report.setDocumentNumber(entity.getDocNumber());
        if (entity.getBillAddr().getLine1() != null) report.setCustomerName(entity.getCustomerRef().getName());
        StringBuilder address = getAddress(entity);
        report.setShipToAddress(address.toString());
        if (entity.getLine() == null) {
            reportDAO.save(report);
            return null;
        }
        List<LineItem> lineItemList = getLineItems(entity);
        report.setLineItems(lineItemList);
        return report;
    }

    private List<LineItem> getLineItems(SalesTransaction entity) {
        List<LineItem> lineItemList = new ArrayList<>();
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
        return lineItemList;
    }

    private StringBuilder getAddress(SalesTransaction entity) {
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
        return address;
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
            DataService service = getDataService();
            String query = "SELECT * FROM TaxCode";
            QueryResult queryResult = service.executeQuery(query);
            List<TaxCode> stateTaxCodes = (List<TaxCode>) queryResult.getEntities();
            TaxCode taxCode = null;
            for (TaxCode code : stateTaxCodes) {
                if ((code != null) && (code.getSalesTaxRateList() != null) && (code.getSalesTaxRateList().getTaxRateDetail() != null) &&
                        (code.getSalesTaxRateList().getTaxRateDetail().get(0) != null) &&
                        (code.getSalesTaxRateList().getTaxRateDetail().get(0).getTaxRateRef() != null) &&
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
        DataService service = getDataService();
        TaxAgency taxAgency = getTaxAgency(service);
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

    private TaxAgency getTaxAgency(DataService service) throws FMSException {
        TaxAgency taxAgency = new TaxAgency();
        TaxAgency taxagency = GenerateQuery.createQueryEntity(TaxAgency.class);
        String query = select($(taxagency)).generate();
        query = query.replaceAll("tring.", "");
        QueryResult queryResult = service.executeQuery(query);
        if (queryResult != null) taxAgency = (TaxAgency) queryResult.getEntities().get(0);
        return taxAgency;
    }

    private String getTaxRateId(BigDecimal taxRate) throws FMSException {
        List<com.intuit.ipp.data.TaxRate> taxRates = getDataService().findAll(new com.intuit.ipp.data.TaxRate());
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

    private List<SalesTransaction> getEntityFromQuickBooks(Entity entity) throws FMSException, InstantiationException, IllegalAccessException {
        DataService dataService = getDataService();
        T queryEntity = GenerateQuery.createQueryEntity(entityClass.newInstance());
        String query = select($(queryEntity)).where($(queryEntity.getId()).eq(entity.getId())).generate();
        query = query.replaceAll("tring.", "");
        QueryResult result = dataService.executeQuery(query);
        return (List<SalesTransaction>) result.getEntities();
    }

    private static Boolean isEqualLines(List<Line> quickBookslines, List<LineItem> actualLines) {
        if (quickBookslines.size() - actualLines.size() != 0) return false;
        for (int i = 0; i < quickBookslines.size(); i++) {
            if (quickBookslines.get(i).getDetailType() == LineDetailTypeEnum.SUB_TOTAL_LINE_DETAIL) return false;
            if (quickBookslines.get(i).getAmount().compareTo(actualLines.get(i).getAmount()) == 0) return false;
        }
        return true;
    }

    private void updateDocument(SalesTransaction entity) throws FMSException {
        DataService dataService = getDataService();
        dataService.update(entity);
    }

    private DataService getDataService() throws FMSException {
        return oauthService.getDataService();
    }
}
