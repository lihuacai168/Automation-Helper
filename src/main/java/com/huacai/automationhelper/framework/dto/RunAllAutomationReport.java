package com.huacai.automationhelper.framework.dto;


import java.util.List;

public class RunAllAutomationReport {

    private Boolean allSuccess;

    private List<RunAutomationReportDto> automationResults;

    public Boolean getAllSuccess() {
        return allSuccess;
    }

    public void setAllSuccess(Boolean allSuccess) {
        this.allSuccess = allSuccess;
    }

    public List<RunAutomationReportDto> getAutomationResults() {
        return automationResults;
    }

    public void setAutomationResults(List<RunAutomationReportDto> automationResults) {
        this.automationResults = automationResults;
    }
}
