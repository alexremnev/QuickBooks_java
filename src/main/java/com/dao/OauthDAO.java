package com.dao;

import com.model.Oauth;

/**
 * Represents get and delete operations for {@code Oauth} model.
 */
public interface OauthDAO extends GenericDAO<Oauth> {
    /**
     * Gets Oauth.
     *
     * @return return single record of Oauth from database. If database haven't any records, return new entity of Oauth.
     */
    Oauth get();

    /**
     * Deletes an Oauth from database.
     */
    void delete();
}
