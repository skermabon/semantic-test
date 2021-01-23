package org.talend.hackathon;


import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;
import org.talend.dataquality.common.inference.Metadata;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.api.SemanticProperties;
import org.talend.dataquality.semantic.datamasking.ValueDataMasker;
import org.talend.dataquality.semantic.recognizer.DefaultCategoryRecognizer;
import org.talend.dataquality.semantic.snapshot.DeletableDictionarySnapshot;
import org.talend.dataquality.semantic.statistics.SemanticAnalyzer;
import org.talend.dataquality.semantic.statistics.SemanticType;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
            List<String> values = asList(null, "FR7630006000011234567890189", "FR0812739000402773652693J61", "FR7414508000501371141778U25", "toto");
            ArrayList<String> column = new ArrayList();
            // column name
            column.add("Iban");
            // column values
            column.addAll(values);
            System.out.println("Column " + column);

            // Found the semantic type of the column
            List<SemanticType> typeFrequencies =
                    semanticDiscovery(dictionarySnapshot, true, column);
            SemanticType semanticType = typeFrequencies.get(0);
            String semanticTypeName = semanticType.getSuggestedCategory();
            System.out.println("Found semantic Type " + semanticTypeName);

            // Initiate the masker
            ValueDataMasker masker = new ValueDataMasker(semanticTypeName, "string", dictionarySnapshot);

            DefaultCategoryRecognizer categoryRecognizer = new DefaultCategoryRecognizer(dictionarySnapshot);
            categoryRecognizer.prepare();

            ArrayList<String> results = new ArrayList<>(values.size());
            for (String value: values) {
                if (value == null || value.isEmpty()) {
                    System.out.println("Unmasked value " + value);
                    results.add(value);
                }
                else {
                    // Categories found for this value
                    List<String> categories = Arrays.asList(categoryRecognizer.process(value));
                    // if categories found contains category of the column
                    if (categories.contains(semanticTypeName)) {
                        System.out.println("Value to mask " + value);
                        String result = masker.maskValue(value);
                        results.add(result);
                        System.out.println("Masked value " + masker.maskValue(value));
                    } else {
                        System.out.println("Unmasked value " + value);
                        results.add(value);
                    }
                }
            }
            categoryRecognizer.end();

            System.out.println("Values " + values);
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

//    private static List<Analyzers.Result> validation(DeletableDictionarySnapshot dictionarySnapshot, Boolean hasHeader,
//                                             List<String> lines, DataTypeEnum[] dataTypes, String[] semanticTypeAsArray) {
//        try (SemanticQualityAnalyzer semanticQualityAnalyzer =
//                     new SemanticQualityAnalyzer(dictionarySnapshot, semanticTypeAsArray, true)) {
//            return getResult(lines, hasHeader,
//                    new ValueQualityAnalyzer(new DataTypeQualityAnalyzer(dataTypes), semanticQualityAnalyzer, true));
//        }
//    }
//
//    private static List<Analyzers.Result> getResult(List<String> lines, boolean hasHeader, Analyzer<?>... analyzers) {
//        try (final Analyzer<Analyzers.Result> analyzer = Analyzers.with(analyzers)) {
//            if (hasHeader)
//                lines.remove(0);
//            for (String line : lines) {
//                analyzer.analyze(line.split(";"));
//            }
//            return analyzer.getResult();
//        } catch (Exception e) {
//            throw new RuntimeException(e.getMessage(), e);
//        }
//    }



}
