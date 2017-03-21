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
            return session.load(entityClass, id);

        } catch (Exception e) {
            logger.error("Exception occured when application tried to get entity by id", e.getCause());
            throw e;
        }
    }

    @Override
    public void save(T t) {
        try {
            Session session = this.sessionFactory.getCurrentSession();
            session.save(t);
        } catch (Exception e) {
            logger.error("Exception occured when application tried to create entity", e.getCause());
        }
    }

    @Override
    public void update(T t) {
        if (t == null) throw new IllegalArgumentException();
        try {
            Session session = sessionFactory.getCurrentSession();
            session.update(t);

        } catch (Exception e) {
            logger.error("Exception occured when application tried to update entity", e.getCause());
            throw e;
        }
    }

    @Override
    public void remove(String id) {
        try {
            Session session = sessionFactory.getCurrentSession();
            T entity = get(id);
            if (entity == null) return;
            session.delete(entity);
        } catch (Exception e) {
            logger.error("Exception occured when application tried to delete the object by id", e.getCause());
            throw e;
        }
    }
}
