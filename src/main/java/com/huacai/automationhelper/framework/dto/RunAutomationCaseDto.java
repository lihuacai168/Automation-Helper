package com.huacai.automationhelper.framework.dto;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class RunAutomationCaseDto {

    private String caseName;
    private List<String> tags;
    private JsonNode caseBaseData;
    private JsonNode caseData;
    private JsonNode expectData;

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public JsonNode getCaseBaseData() {
        return caseBaseData;
    }

    public void setCaseBaseData(JsonNode caseBaseData) {
        this.caseBaseData = caseBaseData;
    }

    public JsonNode getCaseData() {
        return caseData;
    }

    public void setCaseData(JsonNode caseData) {
        this.caseData = caseData;
    }

    public JsonNode getExpectData() {
        return expectData;
    }

    public void setExpectData(JsonNode expectData) {
        this.expectData = expectData;
    }
}
