package com.model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "report")
public class Report {

    @Id
    @Column(name = "Id")
    public String Id;

    protected String DocumentNumber;
    protected Date SaleDate;
    protected String CustomerName;
    protected String ShipToAddress;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "ReportId")
    protected List<LineItem> LineItems;

    public void setDocumentNumber(String documentNumber) {
        DocumentNumber = documentNumber;
    }

    public void setSaleDate(Date saleDate) {
        SaleDate = saleDate;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public void setShipToAddress(String shipToAddress) {
        ShipToAddress = shipToAddress;
    }

    public void setLineItems(List<LineItem> lineItems) {
        LineItems = lineItems;
    }

    public List<LineItem> getLineItems() {
        return LineItems;
    }

    public void setId(String id) { Id = id; }
}
