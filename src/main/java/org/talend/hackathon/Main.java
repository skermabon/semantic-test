package org.talend.hackathon;


import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;
import org.talend.dataquality.common.inference.Metadata;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.api.SemanticProperties;
import org.talend.dataquality.semantic.datamasking.ValueDataMasker;
import org.talend.dataquality.semantic.snapshot.DeletableDictionarySnapshot;
import org.talend.dataquality.semantic.statistics.SemanticAnalyzer;
import org.talend.dataquality.semantic.statistics.SemanticType;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

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


            // Discovery - found the type of the column
            List<String> values = asList("FR7630006000011234567890189", "FR0812739000402773652693J61", "FR7414508000501371141778U25");
            ArrayList<String> column = new ArrayList();
            // column name
            column.add("Iban");
            // column values
            column.addAll(values);
            System.out.println("Column " + column);
            List<SemanticType> typeFrequencies =
                    semanticDiscovery(dictionarySnapshot, true, column);

            SemanticType semanticType = typeFrequencies.get(0);
            System.out.println("Found semantic Type " + semanticType.getSuggestedCategory());

            // The semantic type is IBAN
            ValueDataMasker masker = new ValueDataMasker(semanticType.getSuggestedCategory(), "string", dictionarySnapshot);

            ArrayList<String> results = new ArrayList<>(values.size());
            for (String value: values) {
                System.out.println("Value to mask " + value);
                String result = masker.maskValue(value);
                results.add(result);
                System.out.println("Masked value " + masker.maskValue(value));
            }

            System.out.println("Result " + results);
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

    private static List<SemanticType> semanticDiscovery(DeletableDictionarySnapshot dictionarySnapshot, Boolean hasHeader,
                                                List<String> lines) {
        try (SemanticAnalyzer analyzer = new SemanticAnalyzer(dictionarySnapshot)) {
            if (hasHeader) {
                analyzer.setMetadata(Metadata.HEADER_NAME, singletonList(lines.remove(0)));
            }
            lines.forEach(analyzer::analyze);
            return analyzer.getResult();
        }
    }

}
