package org.talend.hackathon;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MaskDataTest {

    @Test
    void testIban() throws Exception {
        List<String> values = asList(null, "FR7630006000011234567890189", "FR0812739000402773652693J61", "FR7414508000501371141778U25", "toto");
        List<String> result = new ArrayList<>(values.size());
        List<String> categoriesToMask = asList("IBAN");

        MaskData maskData = new MaskData("/home/skermabon/tmp", categoriesToMask, "https://tdp.at.cloud.talend.com/", "skermabon@dataprep.com", "Admin123+");
        try {
            maskData.initialize();

            for (String value : values) {
                result.add(maskData.maskDataRandom("Iban", value));
            }
        } finally {
            maskData.release();
        }

        assertEquals(values.get(0), result.get(0));
        assertNotEquals(values.get(1), result.get(1));
        assertNotEquals(values.get(2), result.get(2));
        assertNotEquals(values.get(3), result.get(3));
        assertEquals(values.get(4), result.get(4));
    }

    @Test
    void testMasterCard() throws Exception {
        List<String> values = asList(null, "5111329517077127", "5108337962302580", "5102325872712598", "FR7630006000011234567890189");
        List<String> result = new ArrayList<>(values.size());
        List<String> categoriesToMask = asList("MASTERCARD");

        MaskData maskData = new MaskData("/home/skermabon/tmp", categoriesToMask, "https://tdp.at.cloud.talend.com/", "skermabon@dataprep.com", "Admin123+");
        try {
            maskData.initialize();

            for (String value : values) {
                result.add(maskData.maskDataRandom("Iban", value));
            }
        } finally {
            maskData.release();
        }

        assertEquals(values.get(0), result.get(0));
        assertNotEquals(values.get(1), result.get(1));
        assertNotEquals(values.get(2), result.get(2));
        assertNotEquals(values.get(3), result.get(3));
        assertEquals(values.get(4), result.get(4));
    }

    @Test
    void testTwoSemanticType() throws Exception {
        List<String> values = asList(null, "5111329517077127", "5108337962302580", "5102325872712598", "FR7630006000011234567890189");
        List<String> result = new ArrayList<>(values.size());
        List<String> categoriesToMask = asList("MASTERCARD", "IBAN");

        MaskData maskData = new MaskData("/home/skermabon/tmp", categoriesToMask, "https://tdp.at.cloud.talend.com/", "skermabon@dataprep.com", "Admin123+");
        try {
            maskData.initialize();

            for (String value : values) {
                result.add(maskData.maskDataRandom("Iban", value));
            }
        } finally {
            maskData.release();
        }

        assertEquals(values.get(0), result.get(0));
        assertNotEquals(values.get(1), result.get(1));
        assertNotEquals(values.get(2), result.get(2));
        assertNotEquals(values.get(3), result.get(3));
        assertNotEquals(values.get(4), result.get(4));
    }
}