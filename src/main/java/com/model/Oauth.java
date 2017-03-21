package com.model;


import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@Table(name = "oauth")
@TypeDef(name = "encryptedType", typeClass = EncryptedStringType.class)
public class Oauth {

    @Id
    @Column(name = "RealmId")
    private String RealmId;

    @Type(type="encryptedType")
    private String AccessToken;

    @Type(type = "encryptedType")
    private String AccessTokenSecret;

    public Oauth() {
    }

    public Oauth(String RealmId, String accessToken, String accessTokenSecret) {
        this.RealmId = RealmId;
        AccessToken = accessToken;
        AccessTokenSecret = accessTokenSecret;
    }

    public String getRealmId() {
        return RealmId;
    }

    public String getAccessToken() {
        return AccessToken;
    }

    public String getAccessTokenSecret() {
        return AccessTokenSecret;
    }
}
