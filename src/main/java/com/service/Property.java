package com.service;

import org.springframework.stereotype.Service;

@Service
public class Property {

    private String REQUEST_TOKEN_URL;
    private String ACCESS_TOKEN_URL;
    private String AUTHORIZE_URL;
    private String OAUTH_CONSUMER_KEY;
    private String OAUTH_CONSUMER_SECRET;
    private String OAUTH_CALLBACK_URL;
    private String APP_TOKEN;
    private String WEB_HOOKS_SUBSCRIBED_ENTITIES;
    private String VERIFIER;
    private String ENCRYPTION_KEY;

    public Property(String REQUEST_TOKEN_URL, String ACCESS_TOKEN_URL, String AUTHORIZE_URL,
                    String OAUTH_CONSUMER_KEY, String OAUTH_CONSUMER_SECRET, String OAUTH_CALLBACK_URL,
                    String APP_TOKEN, String WEB_HOOKS_SUBSCRIBED_ENTITIES, String VERIFIER, String ENCRYPTION_KEY) {
        this.REQUEST_TOKEN_URL = REQUEST_TOKEN_URL;
        this.ACCESS_TOKEN_URL = ACCESS_TOKEN_URL;
        this.AUTHORIZE_URL = AUTHORIZE_URL;
        this.OAUTH_CONSUMER_KEY = OAUTH_CONSUMER_KEY;
        this.OAUTH_CONSUMER_SECRET = OAUTH_CONSUMER_SECRET;
        this.OAUTH_CALLBACK_URL = OAUTH_CALLBACK_URL;
        this.APP_TOKEN = APP_TOKEN;
        this.WEB_HOOKS_SUBSCRIBED_ENTITIES = WEB_HOOKS_SUBSCRIBED_ENTITIES;
        this.VERIFIER = VERIFIER;
        this.ENCRYPTION_KEY = ENCRYPTION_KEY;
    }

    public String getREQUEST_TOKEN_URL() {
        return REQUEST_TOKEN_URL;
    }

    public String getACCESS_TOKEN_URL() {
        return ACCESS_TOKEN_URL;
    }

    public String getAUTHORIZE_URL() {
        return AUTHORIZE_URL;
    }

    public String getOAUTH_CONSUMER_KEY() {
        return OAUTH_CONSUMER_KEY;
    }

    public String getOAUTH_CONSUMER_SECRET() {
        return OAUTH_CONSUMER_SECRET;
    }

    public String getOAUTH_CALLBACK_URL() {
        return OAUTH_CALLBACK_URL;
    }

    String getAPP_TOKEN() {
        return APP_TOKEN;
    }

    public String getWEB_HOOKS_SUBSCRIBED_ENTITIES() {
        return WEB_HOOKS_SUBSCRIBED_ENTITIES;
    }

    public String getVERIFIER() {
        return VERIFIER;
    }

    public String getENCRYPTION_KEY() {
        return ENCRYPTION_KEY;
    }
}
