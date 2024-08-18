package com.huacai.automationhelper.framework.dto;

import com.fasterxml.jackson.databind.JsonNode;


import java.util.List;


public class RunAutomationDto {
    private String automationName;
    private Boolean useInputData = false;
    private JsonNode baseData;
    private List<RunAutomationCaseDto> cases;

    public String getAutomationName() {
        return automationName;
    }

    public void setAutomationName(String automationName) {
        this.automationName = automationName;
    }

    public Boolean getUseInputData() {
        return useInputData;
    }

    public void setUseInputData(Boolean useInputData) {
        this.useInputData = useInputData;
    }

    public JsonNode getBaseData() {
        return baseData;
    }

    public void setBaseData(JsonNode baseData) {
        this.baseData = baseData;
    }

    public List<RunAutomationCaseDto> getCases() {
        return cases;
    }

    public void setCases(List<RunAutomationCaseDto> cases) {
        this.cases = cases;
    }
}
