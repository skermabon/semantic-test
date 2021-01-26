package org.talend.hackathon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.talend.dataquality.semantic.model.DQCategory;

import java.util.List;

public class SemanticTypeUtilTest {

    @Test
    public void listSemanticTypes() {
        GetSchema getSchema = new GetSchema("https://tdp.at.cloud.talend.com", "https://tdp.at.cloud.talend.com");
        String bearer = getSchema.getBearer("skermabon@dataprep.com", "Admin123+");

        SemanticTypeUtil semanticTypeUtil = new SemanticTypeUtil("https://tdp.at.cloud.talend.com/");

        List<DQCategory> list = semanticTypeUtil.getList(bearer);

        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() > 10);
    }
}
