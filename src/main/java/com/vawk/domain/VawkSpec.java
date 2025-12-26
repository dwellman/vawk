package com.vawk.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the declarative spec for an AWK program, including description, inputs, outputs,
 * constraints, and examples as captured from prompts or user intent.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VawkSpec {
    private String description;
    private List<String> inputs = new ArrayList<>();
    private List<String> outputs = new ArrayList<>();
    private List<String> constraints = new ArrayList<>();
    private List<String> examples = new ArrayList<>();

    public VawkSpec() {
    }

    public VawkSpec(String description, List<String> inputs, List<String> outputs, List<String> constraints, List<String> examples) {
        this.description = description;
        if (inputs != null) {
            this.inputs = new ArrayList<>(inputs);
        }
        if (outputs != null) {
            this.outputs = new ArrayList<>(outputs);
        }
        if (constraints != null) {
            this.constraints = new ArrayList<>(constraints);
        }
        if (examples != null) {
            this.examples = new ArrayList<>(examples);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs != null ? new ArrayList<>(inputs) : new ArrayList<>();
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<String> outputs) {
        this.outputs = outputs != null ? new ArrayList<>(outputs) : new ArrayList<>();
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints != null ? new ArrayList<>(constraints) : new ArrayList<>();
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples != null ? new ArrayList<>(examples) : new ArrayList<>();
    }

    /**
     * Specs are equal when all sections match.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VawkSpec vawkSpec)) return false;
        return Objects.equals(description, vawkSpec.description) &&
                Objects.equals(inputs, vawkSpec.inputs) &&
                Objects.equals(outputs, vawkSpec.outputs) &&
                Objects.equals(constraints, vawkSpec.constraints) &&
                Objects.equals(examples, vawkSpec.examples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, inputs, outputs, constraints, examples);
    }
}
