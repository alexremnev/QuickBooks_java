package com.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;

@SuppressWarnings("unchecked")
@Transactional
@Repository
public abstract class GenericDAOImpl<T> implements GenericDAO<T> {

    Logger logger;
    private Class<T> entityClass;

    final
    SessionFactory sessionFactory;

    @Autowired
    public GenericDAOImpl(SessionFactory sessionFactory) {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
        logger = LoggerFactory.getLogger(this.getClass());
        this.sessionFactory = sessionFactory;
    }

    @Override
    public T get(String id) {
        try {
            Session session = sessionFactory.getCurrentSession();
            T ob = session.load(entityClass, id);
            return ob;
        } catch (Exception e) {
            logger.error("Exception occured when application got entity by id", e);
            throw e;
        }
    }

    @Override
    public void save(T t) {
        try {
            Session session = this.sessionFactory.getCurrentSession();
            session.save(t);
        } catch (Exception e) {
            logger.error("Exception occured when application created entity", e);
        }
    }

    @Override
    public void update(T t) {
        if (t == null) throw new IllegalArgumentException();
        try {
            Session session = sessionFactory.getCurrentSession();
            session.update(t);

        } catch (Exception e) {
            logger.error("Exception occured when application updated entity", e);
            throw e;
        }
    }

    @Override
    public void delete(String id) {
        try {
            Session session = sessionFactory.getCurrentSession();
            T entity = get(id);
            if (entity == null) return;
            session.delete(entity);
        } catch (Exception e) {
            logger.error("Exception occured when application tried deleted the object by id", e);
            throw e;
        }
    }
}
