package org.talend.hackathon;

import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;
import org.talend.dataquality.common.inference.Metadata;
import org.talend.dataquality.datamasking.FunctionMode;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.api.SemanticProperties;
import org.talend.dataquality.semantic.datamasking.ValueDataMasker;
import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizer;
import org.talend.dataquality.semantic.recognizer.DefaultCategoryRecognizer;
import org.talend.dataquality.semantic.snapshot.DeletableDictionarySnapshot;
import org.talend.dataquality.semantic.statistics.SemanticAnalyzer;
import org.talend.dataquality.semantic.statistics.SemanticType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class MaskData {

    private Path tempDir;

    private DeletableDictionarySnapshot dictionarySnapshot;

    private HashMap<String, ValueDataMasker> valueDataMaskers = new HashMap<>();

    private boolean isInitialize = false;

    DefaultCategoryRecognizer categoryRecognizer;

    private List<String> categoriesToMask;

    private String username;

    private String password;

    private String tdpUrl;

    public MaskData(String dictionaryPath, String tdpUrl, String username, String password) {
        tempDir = (new File(dictionaryPath)).toPath();
        this.username = username;
        this.password = password;
        this.tdpUrl = tdpUrl;
    }

    public String maskDataRandom(String columnName, String value) throws Exception {
        return maskData(columnName, value, FunctionMode.RANDOM, null);
    }

    public String maskData(String columnName, String value, FunctionMode mode, String seed) throws Exception{
        if (!isInitialize) {
            initialize();
        }
        String semanticType = foundSemanticTypeOfColumn(columnName, Collections.singletonList(value), dictionarySnapshot);
        if (categoriesToMask.contains(semanticType)) {
            return maskValue(semanticType, value, FunctionMode.RANDOM, seed, dictionarySnapshot, categoryRecognizer);
        }
        else {
            return value;
        }
    }

    private String maskValue(String semanticType, String entry, FunctionMode mode, String seed, DeletableDictionarySnapshot dictionarySnapshot, CategoryRecognizer categoryRecognizer) {
        if (entry == null || entry.isEmpty()) {
            return entry;
        } else {
            // Categories found for this value
            List<String> categories = Arrays.asList(categoryRecognizer.process(entry));
            if (categories.contains(semanticType)) {
                if (valueDataMaskers.get(semanticType) == null) {
                    valueDataMaskers.put(semanticType, new ValueDataMasker(semanticType, "string", Collections.EMPTY_LIST, dictionarySnapshot, seed, mode));
                }
                ValueDataMasker masker = valueDataMaskers.get(semanticType);
                return masker.maskValue(entry);
            } else {
                return entry;
            }
        }
    }


    private String foundSemanticTypeOfColumn(String columnName, List<String> values, DeletableDictionarySnapshot dictionarySnapshot) {
        ArrayList<String> completeColumn = new ArrayList(values.size() + 1);
        completeColumn.add(columnName);
        completeColumn.addAll(values);

        List<SemanticType> typeFrequencies =
                semanticDiscovery(dictionarySnapshot, true, completeColumn);
        SemanticType semanticType = typeFrequencies.get(0);
        String semanticTypeName = semanticType.getSuggestedCategory();
        return semanticTypeName;
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


    public void initialize() throws Exception {
        initializeCustomIndex();
        initializeDictionary();
        categoryRecognizer = new DefaultCategoryRecognizer(dictionarySnapshot);
        categoryRecognizer.prepare();
        isInitialize = true;

    }

    public void release() {
        if (categoryRecognizer != null) {
            categoryRecognizer.end();
        }
        if (dictionarySnapshot != null) {
            dictionarySnapshot.release();
        }
        try {
            FileUtils.deleteDirectory(new File(tempDir.toString() + "/tenantId"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeDictionary() {
        SemanticProperties properties = new SemanticProperties(tempDir.toString());
        CategoryRegistryManager categoryRegistryManager = new CategoryRegistryManager(properties);
        dictionarySnapshot =
                categoryRegistryManager.getCustomDictionaryHolder("tenantId").getDeletableDictionarySnapshot().bind();

        GetSchema getSchema = new GetSchema(tdpUrl, tdpUrl);
        String bearer = getSchema.getBearer(username, password);

        SemanticTypeUtil semanticTypeUtil = new SemanticTypeUtil(tdpUrl);

        // Get the list
        List<DQCategory> list = semanticTypeUtil.getList(bearer);

        System.out.println("Number " + categoryRegistryManager.getCustomDictionaryHolder().listCategories().size());

        for (DQCategory category: list) {
            DQCategory existingCategory = categoryRegistryManager.getCustomDictionaryHolder().getCategoryMetadataByName(category.getName());
            if (existingCategory == null) {
                 categoryRegistryManager.getCustomDictionaryHolder().createCategory(category);
            }
        }

        System.out.println("Number " + categoryRegistryManager.getCustomDictionaryHolder().listCategories().size());

        categoriesToMask = list.stream().map(c -> c.getName()).collect(Collectors.toList());

        dictionarySnapshot =
                categoryRegistryManager.getCustomDictionaryHolder("tenantId").getDeletableDictionarySnapshot().bind();
    }

    private void initializeCustomIndex() throws Exception {
        System.out.println("initCustomIndex start");

        final URL customIndex = Main.class.getClassLoader().getResource("customIndex");
        final Path destination = tempDir.resolve("tenantId");
        final Path from = Paths.get(customIndex.toURI());
        Files.walk(from).forEach(Unchecked.consumer(it -> Files.copy(it, destination.resolve(from.relativize(it)))));

        System.out.println("initCustomIndex end");
    }
}
