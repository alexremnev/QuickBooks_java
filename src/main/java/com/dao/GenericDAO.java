package com.dao;

/**
 * Represents base CRUD operations.
 *
 * @param <T> the type of persistance object
 */
public interface GenericDAO<T> {
    /**
     * Saves entity in database.
     *
     * @param t the entity which is saved in database.
     */
    void save(T t);

    /**
     * Gets entity by id.
     *
     * @param id entity id.
     * @return found entity or null in case there's no entity with passed id found.
     */
    T get(String id);

    /**
     * Updates an entity. If t is null occurs IllegalArgumentException.
     *
     * @param t persistant entity.
     */
    void update(T t);

    /**
     * Deletes an entity.
     *
     * @param id entity id.
     */
    void delete(String id);
}
