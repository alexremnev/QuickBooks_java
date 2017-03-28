package com.service;

import com.intuit.ipp.data.Entity;

/**
 * Represent operations for calculating sales tax in {@code com.intuit.ipp.data.SalesTransaction}'s entities.
 */
public interface Calculable {
    /**
     * Recalculates sales tax in all entities.
     */
    void calculate();

    /**
     * Recalculates  sales tax from entities which comes from webhooks.
     *
     * @param entity entity of {@code com.intuit.ipp.data.Entity}.
     */

    void process(Entity entity);

    /**
     * Return name of entity.
     *
     * @return name of entity.
     */
    String getEntityName();
}
