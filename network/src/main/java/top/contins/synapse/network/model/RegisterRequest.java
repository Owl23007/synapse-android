package top.contins.synapse.network.model;

public class RegisterRequest {
    public String username;

    public String email;

    public String password;

    public String captchaId;

    public String captchaCode;

    public RegisterRequest(String username, String email, String password, String captchaId, String captchaCode) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.captchaId = captchaId;
        this.captchaCode = captchaCode;
    }
}
