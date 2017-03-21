package com.dao;

import com.model.Oauth;

public interface OauthDAO extends GenericDAO<Oauth> {
    Oauth get();
    void remove();
}
