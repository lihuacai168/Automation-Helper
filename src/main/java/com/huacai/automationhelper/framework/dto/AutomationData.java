package com.huacai.automationhelper.framework.dto;

import com.fasterxml.jackson.databind.JsonNode;


public class AutomationData {

    private String caseName;

    private JsonNode baseData;

    private JsonNode caseBaseData;

    private JsonNode caseData;

    private JsonNode expectData;

    public JsonNode getExpectData() {
        return expectData;
    }

    public void setExpectData(JsonNode expectData) {
        this.expectData = expectData;
    }

    public JsonNode getCaseData() {
        return caseData;
    }

    public void setCaseData(JsonNode caseData) {
        this.caseData = caseData;
    }

    public JsonNode getCaseBaseData() {
        return caseBaseData;
    }

    public void setCaseBaseData(JsonNode caseBaseData) {
        this.caseBaseData = caseBaseData;
    }

    public JsonNode getBaseData() {
        return baseData;
    }

    public void setBaseData(JsonNode baseData) {
        this.baseData = baseData;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }
}
