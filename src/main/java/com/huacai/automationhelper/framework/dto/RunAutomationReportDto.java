package com.huacai.automationhelper.framework.dto;


import java.util.List;


public class RunAutomationReportDto {

    private String name;
    private String target;
    private String description;
    private String className;
    private Boolean allSuccess;
    private List<RunAutomationCaseReport> reports;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Boolean getAllSuccess() {
        return allSuccess;
    }

    public void setAllSuccess(Boolean allSuccess) {
        this.allSuccess = allSuccess;
    }

    public List<RunAutomationCaseReport> getReports() {
        return reports;
    }

    public void setReports(List<RunAutomationCaseReport> reports) {
        this.reports = reports;
    }
}
