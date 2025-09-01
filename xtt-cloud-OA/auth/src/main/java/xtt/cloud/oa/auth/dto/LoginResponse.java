package xtt.cloud.oa.auth.dto;

/**
 * Login Response DTO
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
public class LoginResponse {
    
    private String token;
    private String refreshToken;
    private String username;
    private String role;
    private String tokenType = "Bearer";
    private Long expiresIn;

    public LoginResponse() {}

    public LoginResponse(String token, String refreshToken, String username, String role, Long expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
