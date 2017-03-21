package com.dao;

import com.model.Report;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ReportDAOImpl extends GenericDAOImpl<Report> implements ReportDAO {
    public ReportDAOImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

}
