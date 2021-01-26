package org.talend.hackathon;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.type.TypeReference;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.talend.dataquality.semantic.model.CategoryPrivacyLevel;
import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.model.DQDocument;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SemanticTypeUtil {

    private DefaultHttpClient httpClient = new DefaultHttpClient();

    private String tdpUrl = "https://tdp.at.cloud.talend.com/";

    private ObjectMapper objectMapper = new ObjectMapper();

    public SemanticTypeUtil(String tdpUrl) {
        this.tdpUrl = tdpUrl;
    }

    public List<DQCategory> getList(String bearer) {
        HttpResponse response = null;
        HttpGet request = null;
        try {
            URI uri = new URIBuilder(tdpUrl + "/dq/semanticservice/categories").build();
            request = new HttpGet(uri);
            request.addHeader(HttpHeaders.AUTHORIZATION, bearer);

            response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                String entity = IOUtils.toString(response.getEntity().getContent(), "UTF8");
                List<DQCategory> semanticTypes = objectMapper.readValue(
                        entity, new TypeReference<List<DQCategory>>() {
                        });

                semanticTypes = semanticTypes.stream() //
                        .filter(c -> c.getName().startsWith("hack21_ctv")) //
                        .filter(c -> c.getPrivacyLevel().equals(CategoryPrivacyLevel.PII)).collect(Collectors.toList());

                return semanticTypes;
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (request != null) {
                request.releaseConnection();
            }
        }
        return null;
    }

    public List<DQDocument> completeSemanticType(DQCategory semanticType, String bearer) {
        HttpResponse response = null;
        HttpGet request = null;
        URI uri = null;
        try {
            uri = new URIBuilder(tdpUrl.replace("tdp", "tds") +  "/semanticservice/documents").addParameter("category", semanticType.getId()).build();
            request = new HttpGet(uri);
            request.addHeader(HttpHeaders.AUTHORIZATION, bearer);
            request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            request.addHeader(HttpHeaders.ACCEPT, "*/*");

            response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                String entity = IOUtils.toString(response.getEntity().getContent(), "UTF8");
                objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                List<DQDocument> export = objectMapper.readValue(
                        entity, new TypeReference<List<DQDocument>>() {
                        });
                export.stream().forEach(d -> d.setCategory(semanticType));

                return export;
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (request != null) {
                request.releaseConnection();
            }
        }
        return null;

    }

}
