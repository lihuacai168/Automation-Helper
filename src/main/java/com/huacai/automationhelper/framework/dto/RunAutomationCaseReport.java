package com.huacai.automationhelper.framework.dto;


import java.util.ArrayList;
import java.util.List;

public class RunAutomationCaseReport {

    public static final String STEP_BASE_DATA_INIT = "baseDataInit";
    public static final String STEP_CASE_BASE_DATA_INIT = "caseBaseDataInit";
    public static final String STEP_CASE_RUN = "caseRun";
    public static final String STEP_CASE_ASSERT = "caseAssert";
    public static final String STEP_CASE_BASE_DATA_CLEAN = "caseBaseDataClean";
    public static final String STEP_DATA_CLEAN = "dataClean";

    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_FAIL = "fail";

    private String caseName;


    private Boolean allSuccess;

    private List<RunAutomationStepReport> steps = new ArrayList<>();

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public Boolean getAllSuccess() {
        return allSuccess;
    }

    public void setAllSuccess(Boolean allSuccess) {
        this.allSuccess = allSuccess;
    }

    public List<RunAutomationStepReport> getSteps() {
        return steps;
    }

    public void setSteps(List<RunAutomationStepReport> steps) {
        this.steps = steps;
    }
}
