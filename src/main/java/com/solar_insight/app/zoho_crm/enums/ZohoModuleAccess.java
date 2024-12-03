package com.solar_insight.app.zoho_crm.enums;

public enum ZohoModuleAccess {

    CUSTOM_MODULE("custom_module"),
    FILE_UPLOAD("file_upload"),
    LEADS("Leads");

    private final String module;

    ZohoModuleAccess(String module) { this.module = module; }

    @Override
    public String toString() { return module; }

}
