package com.finance.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /** When false, platform OPENAI_API_KEY is never used (user keys still work). */
    private boolean platformEnabled = false;

    /** Minimum account age before platform AI is allowed (hours). */
    private int platformMinAccountAgeHours = 24;

    /** Block seeded trial user from consuming platform AI. */
    private boolean blockDemoUserPlatformAi = true;

    private int maxDescriptionLength = 200;

    private int quotaWindowSeconds = 3600;

    private int userCategoryQuotaPerWindow = 60;
    private int userReceiptQuotaPerWindow = 15;
    private int userMonthlyBriefQuotaPerWindow = 12;

    private int platformCategoryQuotaPerWindow = 25;
    private int platformReceiptQuotaPerWindow = 5;
    private int platformMonthlyBriefQuotaPerWindow = 6;

    public boolean isPlatformEnabled() {
        return platformEnabled;
    }

    public void setPlatformEnabled(boolean platformEnabled) {
        this.platformEnabled = platformEnabled;
    }

    public int getPlatformMinAccountAgeHours() {
        return platformMinAccountAgeHours;
    }

    public void setPlatformMinAccountAgeHours(int platformMinAccountAgeHours) {
        this.platformMinAccountAgeHours = Math.max(0, platformMinAccountAgeHours);
    }

    public boolean isBlockDemoUserPlatformAi() {
        return blockDemoUserPlatformAi;
    }

    public void setBlockDemoUserPlatformAi(boolean blockDemoUserPlatformAi) {
        this.blockDemoUserPlatformAi = blockDemoUserPlatformAi;
    }

    public int getMaxDescriptionLength() {
        return maxDescriptionLength;
    }

    public void setMaxDescriptionLength(int maxDescriptionLength) {
        this.maxDescriptionLength = Math.max(32, maxDescriptionLength);
    }

    public int getQuotaWindowSeconds() {
        return quotaWindowSeconds;
    }

    public void setQuotaWindowSeconds(int quotaWindowSeconds) {
        this.quotaWindowSeconds = Math.max(60, quotaWindowSeconds);
    }

    public int getUserCategoryQuotaPerWindow() {
        return userCategoryQuotaPerWindow;
    }

    public void setUserCategoryQuotaPerWindow(int userCategoryQuotaPerWindow) {
        this.userCategoryQuotaPerWindow = Math.max(1, userCategoryQuotaPerWindow);
    }

    public int getUserReceiptQuotaPerWindow() {
        return userReceiptQuotaPerWindow;
    }

    public void setUserReceiptQuotaPerWindow(int userReceiptQuotaPerWindow) {
        this.userReceiptQuotaPerWindow = Math.max(1, userReceiptQuotaPerWindow);
    }

    public int getUserMonthlyBriefQuotaPerWindow() {
        return userMonthlyBriefQuotaPerWindow;
    }

    public void setUserMonthlyBriefQuotaPerWindow(int userMonthlyBriefQuotaPerWindow) {
        this.userMonthlyBriefQuotaPerWindow = Math.max(1, userMonthlyBriefQuotaPerWindow);
    }

    public int getPlatformCategoryQuotaPerWindow() {
        return platformCategoryQuotaPerWindow;
    }

    public void setPlatformCategoryQuotaPerWindow(int platformCategoryQuotaPerWindow) {
        this.platformCategoryQuotaPerWindow = Math.max(1, platformCategoryQuotaPerWindow);
    }

    public int getPlatformReceiptQuotaPerWindow() {
        return platformReceiptQuotaPerWindow;
    }

    public void setPlatformReceiptQuotaPerWindow(int platformReceiptQuotaPerWindow) {
        this.platformReceiptQuotaPerWindow = Math.max(1, platformReceiptQuotaPerWindow);
    }

    public int getPlatformMonthlyBriefQuotaPerWindow() {
        return platformMonthlyBriefQuotaPerWindow;
    }

    public void setPlatformMonthlyBriefQuotaPerWindow(int platformMonthlyBriefQuotaPerWindow) {
        this.platformMonthlyBriefQuotaPerWindow = Math.max(1, platformMonthlyBriefQuotaPerWindow);
    }
}
