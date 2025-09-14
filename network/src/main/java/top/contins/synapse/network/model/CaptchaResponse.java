package top.contins.synapse.network.model;

public class CaptchaResponse {
    private String captchaId;
    private String captchaImage; // base64编码的图片

    public CaptchaResponse() {}

    public CaptchaResponse(String captchaId, String captchaImage, long expireTime) {
        this.captchaId = captchaId;
        this.captchaImage = captchaImage;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptchaImage() {
        return captchaImage;
    }

    public void setCaptchaImage(String captchaImage) {
        this.captchaImage = captchaImage;
    }
}

