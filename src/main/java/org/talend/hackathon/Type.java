package org.talend.hackathon;

public class Type {
    public String getDqType() {
        return dqType;
    }

    public void setDqType(String dqType) {
        this.dqType = dqType;
    }

    private String dqType;

    @Override
    public String toString() {
        return "Type{" +
                "dqType='" + dqType + '\'' +
                ", dataType='" + dataType + '\'' +
                ", dqTypeId='" + dqTypeId + '\'' +
                '}';
    }

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
