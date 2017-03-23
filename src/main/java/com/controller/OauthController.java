package com.controller;

import com.dao.OauthDAO;
import com.model.Oauth;
import com.util.Property;
import oauth.signpost.*;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.http.HttpParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


@Controller
public class OauthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OauthController.class);

    private OauthDAO oauthDAO;

    @Autowired
    public OauthController(OauthDAO oauthDAO) {
        this.oauthDAO = oauthDAO;
    }

    @RequestMapping(value = "/start_oauth.htm", method = RequestMethod.GET)
    public String start_oauth(final HttpServletRequest request, HttpServletResponse response) {
        try {
            OAuthProvider provider = getOAuthProvider();
            OAuthConsumer oAuthConsumer = getOAuthConsumer();
            String authUrl = getOauthUrl(provider, oAuthConsumer);
            HttpSession session = request.getSession();
            setAttributes(oAuthConsumer, session);
            response.sendRedirect(authUrl);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception occured when application tried to get request token", e);
        }
        return "index";
    }

    @RequestMapping(value = "/accessToken.htm")
    public String getTokens(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String realmID = request.getParameter("realmId");
            session.setAttribute("realmId", realmID);
            OAuthConsumer authConsumer = (OAuthConsumer) session.getAttribute("oauthConsumer");
            setAdditionalParametrs(request, authConsumer);
            URL url = getUrl(authConsumer);
            HttpURLConnection urlConnection = getHttpURLConnection(url);
            urlConnection.setRequestMethod("GET");
            getAccessTokenAndSecret(realmID, urlConnection);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception occured when application got access token and secret", e);
        }
        return "close";
    }

    private String getOauthUrl(OAuthProvider provider, OAuthConsumer oAuthConsumer) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
        return provider.retrieveRequestToken(oAuthConsumer, Property.OAUTH_CALLBACK_URL);
    }

    private URL getUrl(OAuthConsumer oauthconsumer) throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, MalformedURLException {
        String signedURL = oauthconsumer.sign(Property.ACCESS_TOKEN_URL);
        return new URL(signedURL);
    }

    private void setAttributes(OAuthConsumer oAuthConsumer, HttpSession session) {
        session.setAttribute("requestToken", oAuthConsumer.getToken());
        session.setAttribute("requestTokenSecret", oAuthConsumer.getTokenSecret());
        session.setAttribute("oauthConsumer", oAuthConsumer);
    }

    private void getAccessTokenAndSecret(String realmID, HttpURLConnection urlConnection) throws IOException {
        StringBuilder sb = readInputStream(urlConnection);
        String acceessTokenResponse = sb.toString();
        String[] responseElements = acceessTokenResponse.split("&");
        if (responseElements.length > 1) {
            String accessToken = responseElements[1].split("=")[1];
            String accessTokenSecret = responseElements[0].split("=")[1];
            updateAccessTokenAndSecret(realmID, accessToken, accessTokenSecret);
        }
    }

    private StringBuilder readInputStream(HttpURLConnection urlConnection) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        return sb;
    }

    private void updateAccessTokenAndSecret(String realmID, String accessToken, String accessTokenSecret) {
        oauthDAO.delete();
        oauthDAO.save(new Oauth(realmID, accessToken, accessTokenSecret));
    }

    private HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    private AbstractOAuthConsumer getOAuthConsumer() {
        return new DefaultOAuthConsumer(Property.OAUTH_CONSUMER_KEY, Property.OAUTH_CONSUMER_SECRET);
    }

    private AbstractOAuthProvider getOAuthProvider() {
        return new DefaultOAuthProvider(Property.REQUEST_TOKEN_URL, Property.ACCESS_TOKEN_URL, Property.AUTHORIZE_URL);
    }

    private void setAdditionalParametrs(HttpServletRequest request, OAuthConsumer oauthconsumer) {
        HttpParameters additionalParams = new HttpParameters();
        additionalParams.put("oauth_callback", OAuth.OUT_OF_BAND);
        additionalParams.put("oauth_verifier", request.getParameter("oauth_verifier"));
        oauthconsumer.setAdditionalParameters(additionalParams);
    }

}
