package com.dao;

import com.model.TaxRate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
@SuppressWarnings("unchecked")
public class TaxRateDAOImpl extends GenericDAOImpl<TaxRate> implements TaxRateDAO {

    @Autowired
    public TaxRateDAOImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<TaxRate> list() {
        try {
            Session session = this.sessionFactory.getCurrentSession();
            return (List<TaxRate>) session.createQuery("from TaxRate ").getResultList();
        } catch (Exception e) {
            logger.error("Exception occured when application got list of tax rates", e);
            throw e;
        }
    }

    public TaxRate getByCountrySubDivisionCode(String state) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Query query = session.createQuery("FROM TaxRate WHERE CountrySubDivisionCode = " + state);
            return (TaxRate) query.getSingleResult();
        } catch (Exception e) {
            logger.error("Exception occured when application got the tax rate by country subdivision code", e);
            throw e;
        }
    }
}

