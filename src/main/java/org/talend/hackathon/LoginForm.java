package org.talend.hackathon;

public class LoginForm {

    public LoginForm(String username, String password) {
        this.username = username;
        this.password = password;
    }

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;
}
