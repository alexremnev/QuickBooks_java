package com.dao;

public interface GenericDAO<T> {
    void save(T t);

    T get(String id);

    void update(T t);

    void remove(String id);
}
