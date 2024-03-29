package de.wichura.lks.models;

/**
 * Created by bwichura on 24.02.2017.
 * blue ground
 */

public class User {

    private String id;
    private String email;
    private String password;
    private String token;
    private String activationCode;
    private String name;
    private String profilePictureUrl;
    private Integer numberOfArticles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Integer getNumberOfArticles() {
        return numberOfArticles;
    }

    public void setNumberOfArticles(Integer numberOfArticles) {
        this.numberOfArticles = numberOfArticles;
    }
}
