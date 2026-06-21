package com.iotroom.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenDTO {

    @NotBlank(message = "O refresh token é obrigatório")
    private String refreshToken;

    private String appClient;
    private String deviceId;

    public RefreshTokenDTO() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAppClient() {
        return appClient;
    }

    public void setAppClient(String appClient) {
        this.appClient = appClient;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}