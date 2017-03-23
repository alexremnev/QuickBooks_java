package com.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

@Component
public class Property {

    private static final Logger LOGGER = LoggerFactory.getLogger(Property.class);

    public static String REQUEST_TOKEN_URL;
    public static String ACCESS_TOKEN_URL;
    public static String AUTHORIZE_URL;
    public static String OAUTH_CONSUMER_KEY;
    public static String OAUTH_CONSUMER_SECRET;
    public static String OAUTH_CALLBACK_URL;
    public static String APP_TOKEN;
  //  public static String WEB_HOOKS_SUBSCRIBED_ENTITIES;
    public static String VERIFIER;
    public static String ENCRYPTION_KEY;

    static {
        try {
            String PROP_FILE = "qb.properties";
            Properties propConfig = PropertiesLoaderUtils
                    .loadProperties(new ClassPathResource(PROP_FILE));
            REQUEST_TOKEN_URL = propConfig.getProperty("REQUEST_TOKEN_URL");

            ACCESS_TOKEN_URL = propConfig.getProperty("ACCESS_TOKEN_URL");
            AUTHORIZE_URL = propConfig.getProperty("AUTHORIZE_URL");
            OAUTH_CONSUMER_KEY = propConfig.getProperty("OAUTH_CONSUMER_KEY");
            OAUTH_CONSUMER_SECRET = propConfig.getProperty("OAUTH_CONSUMER_SECRET");
            OAUTH_CALLBACK_URL = propConfig.getProperty("OAUTH_CALLBACK_URL");
            APP_TOKEN = propConfig.getProperty("APP_TOKEN");
     //       WEB_HOOKS_SUBSCRIBED_ENTITIES = propConfig.getProperty("WEB_HOOKS_SUBSCRIBED_ENTITIES");
            VERIFIER = propConfig.getProperty("VERIFIER");
            ENCRYPTION_KEY = propConfig.getProperty("ENCRYPTION_KEY");

        } catch (IOException e) {
            LOGGER.error("Properties file can not be loaded.", e);
        }
    }
}
