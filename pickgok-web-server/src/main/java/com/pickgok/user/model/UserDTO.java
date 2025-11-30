package com.pickgok.user.model;

public class UserDTO {
    private String userid;
    private String password;
    private String name;
    private String email;
    // [추가] 선호 장르 필드
    private String preferredGenres; 

    public UserDTO() {}
    
    // Getters & Setters
    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPreferredGenres() { return preferredGenres; }
    public void setPreferredGenres(String preferredGenres) { this.preferredGenres = preferredGenres; }
}