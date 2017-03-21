package com.dao;

import com.model.Oauth;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import java.util.List;

@SuppressWarnings("unchecked")
@Repository
public class OauthDAOImpl extends GenericDAOImpl<Oauth> implements OauthDAO {

    public OauthDAOImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Oauth get() {
        try {
            Session session = this.sessionFactory.getCurrentSession();
            List<Oauth> oauthList = session.createQuery("from Oauth").getResultList();
            if (oauthList.size() != 0) return oauthList.get(0);
            return new Oauth();
        } catch (Exception e) {
            logger.error("Exception occured when application tried to get entity", e.getCause());
            throw e;
        }
    }

    @Override
    public void remove() {
        try {
            Oauth oauth = get();
            Session session = this.sessionFactory.getCurrentSession();
            if (oauth == null) return;
            session.delete(oauth);
        } catch (Exception e) {
            logger.error("Exception occured when application tried to delete the object", e.getCause());
            throw e;
        }
    }
}
