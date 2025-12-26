package com.vawk.ai;

/**
 * Holds PLAN/CODE/TESTS/NOTES sections returned from an AI call.
 */
public class AiResponse {
    private String plan;
    private String code;
    private String tests;
    private String notes;

    public AiResponse() {
    }

    public AiResponse(String plan, String code, String tests, String notes) {
        this.plan = plan;
        this.code = code;
        this.tests = tests;
        this.notes = notes;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTests() {
        return tests;
    }

    public void setTests(String tests) {
        this.tests = tests;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
