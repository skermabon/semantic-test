package org.talend.hackathon;

import java.util.List;

public class Schema {

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    private List<Field> fields;
}
