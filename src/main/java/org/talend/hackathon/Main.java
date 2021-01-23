package org.talend.hackathon;


import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.api.SemanticProperties;
import org.talend.dataquality.semantic.datamasking.ValueDataMasker;
import org.talend.dataquality.semantic.snapshot.DeletableDictionarySnapshot;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static Path tempDir = (new File("/home/skermabon/tmp")).toPath();

    public static void main(String[] args) throws Exception {
        System.out.println("Hackathon start");

        DeletableDictionarySnapshot dictionarySnapshot = null;

        try {
            initCustomIndex();
            System.out.println("Get Dictionary");
            SemanticProperties properties = new SemanticProperties(tempDir.toString());
            CategoryRegistryManager categoryRegistryManager = new CategoryRegistryManager(properties);
            dictionarySnapshot =
                    categoryRegistryManager.getCustomDictionaryHolder("tenantId").getDeletableDictionarySnapshot().bind();


            // The semantic type is IBAN
            ValueDataMasker masker = new ValueDataMasker("IBAN", "string", dictionarySnapshot);

            String value = "FR7630006000011234567890189";
            System.out.println("Value to mask " + value);

            String maskedValue = masker.maskValue(value);
            System.out.println("Masked value " + maskedValue);
        }
        finally {
            dictionarySnapshot.release();
            FileUtils.deleteDirectory(new File("/home/skermabon/tmp/tenantId"));
        }

        System.out.println("Hackathon end");
    }

    private static void initCustomIndex() throws Exception {
        System.out.println("initCustomIndex start");

        final URL customIndex = Main.class.getClassLoader().getResource("customIndex");
        final Path destination = tempDir.resolve("tenantId");
        final Path from = Paths.get(customIndex.toURI());
        Files.walk(from).forEach(Unchecked.consumer(it -> Files.copy(it, destination.resolve(from.relativize(it)))));

        System.out.println("initCustomIndex end");
    }

}
