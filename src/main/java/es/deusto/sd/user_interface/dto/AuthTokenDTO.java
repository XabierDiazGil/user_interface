package es.deusto.sd.user_interface.dto;

public class AuthTokenDTO {
    private String token;

    public AuthTokenDTO() {}

    public AuthTokenDTO(String token) {
        this.token = token;
    }

    private Integer userId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

}
