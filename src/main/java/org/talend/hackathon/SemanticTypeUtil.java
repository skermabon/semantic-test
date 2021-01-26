package org.talend.hackathon;

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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

}
