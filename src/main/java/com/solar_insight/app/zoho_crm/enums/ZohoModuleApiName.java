package com.solar_insight.app.zoho_crm.enums;

public enum ZohoModuleApiName {

    SOLAR_INSIGHT_LEADS("Solar_Insight_Leads"),
    FILE_UPLOAD("files");

    private final String apiName;

    ZohoModuleApiName(String apiName) { this.apiName = apiName; }

    @Override
    public String toString() { return apiName; }

}
