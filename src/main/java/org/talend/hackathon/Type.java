package org.talend.hackathon;

public class Type {

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDqTypeId() {
        return dqTypeId;
    }

    public void setDqTypeId(String dqTypeId) {
        this.dqTypeId = dqTypeId;
    }

    private String dataType;

    private String dqTypeId;
}
