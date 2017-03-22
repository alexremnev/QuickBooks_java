package com.controller;

import com.dao.OauthDAO;
import com.model.Oauth;
import com.service.Property;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


@Controller
public class OauthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OauthController.class);

    private Property property;
    private OauthDAO oauthDAO;

    @Autowired
    public OauthController(Property property, OauthDAO oauthDAO) {
        this.property = property;
        this.oauthDAO = oauthDAO;
    }

    @RequestMapping(value = "/start_oauth.htm", method = RequestMethod.GET)
    public String start_oauth(final HttpServletRequest request, HttpServletResponse response) {
        OAuthConsumer oAuthConsumer;
        OAuthProvider provider = new DefaultOAuthProvider(property.getREQUEST_TOKEN_URL(), property.getACCESS_TOKEN_URL(), property.getAUTHORIZE_URL());
        try {
            oAuthConsumer = new DefaultOAuthConsumer(property.getOAUTH_CONSUMER_KEY(), property.getOAUTH_CONSUMER_SECRET());
            String authUrl = provider.retrieveRequestToken(oAuthConsumer, property.getOAUTH_CALLBACK_URL());
            HttpSession session = request.getSession();
            session.setAttribute("requestToken", oAuthConsumer.getToken());
            session.setAttribute("requestTokenSecret", oAuthConsumer.getTokenSecret());
            session.setAttribute("oauthConsumer", oAuthConsumer);
            response.sendRedirect(authUrl);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception occured when application tried to get request token", e.getCause());
        }
        return "index";
    }

    @RequestMapping(value = "/accessToken.htm")
    public String accessToken(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String realmID = request.getParameter("realmId");
            session.setAttribute("realmId", realmID);
            OAuthConsumer oauthconsumer = (OAuthConsumer) session.getAttribute("oauthConsumer");
            HttpParameters additionalParams = new HttpParameters();
            additionalParams.put("oauth_callback", OAuth.OUT_OF_BAND);
            additionalParams.put("oauth_verifier", request.getParameter("oauth_verifier"));
            oauthconsumer.setAdditionalParameters(additionalParams);
            String signedURL = oauthconsumer.sign(property.getACCESS_TOKEN_URL());
            URL url = new URL(signedURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            String acceessTokenResponse;
            String accessToken;
            String accessTokenSecret;

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            acceessTokenResponse = sb.toString();
            String[] responseElements = acceessTokenResponse.split("&");
            if (responseElements.length > 1) {
                accessToken = responseElements[1].split("=")[1];
                accessTokenSecret = responseElements[0].split("=")[1];
                oauthDAO.delete();
                oauthDAO.save(new Oauth(realmID, accessToken, accessTokenSecret));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception occured when application tried to get access token and secret", e.getCause());
        }
        return "close";
    }
}
