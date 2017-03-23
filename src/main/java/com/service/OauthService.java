package com.service;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.core.ServiceType;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.security.OAuthAuthorizer;
import com.intuit.ipp.services.DataService;
import com.dao.OauthDAO;
import com.model.Oauth;
import com.util.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OauthService {

    private OauthDAO oauthDAO;

    @Autowired
    public OauthService(OauthDAO oauthDAO) {
        this.oauthDAO = oauthDAO;
    }

    DataService getDataService() throws FMSException {
        Context context = getContext();
        return new DataService(context);
    }

    Context getContext() throws FMSException {
        Oauth oauth = oauthDAO.get();
        OAuthAuthorizer authorizer = new OAuthAuthorizer(Property.OAUTH_CONSUMER_KEY, Property.OAUTH_CONSUMER_SECRET, oauth.getAccessToken(), oauth.getAccessTokenSecret());
        return new Context(authorizer, Property.APP_TOKEN, ServiceType.QBO, oauth.getRealmId());
    }
}
