package org.talend.hackathon;


import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class GetSchema {

    private DefaultHttpClient httpClient = new DefaultHttpClient();

    private String dataInventoryUrl = "https://tdp.at.cloud.talend.com";

    private String tdpLoginUrl = "https://tdp.at.cloud.talend.com";



    public GetSchema(String dataInventoryUrl, String tdpLoginUrl) {
        this.dataInventoryUrl = dataInventoryUrl;
        this.tdpLoginUrl = tdpLoginUrl;
    }

    public String getBearer(String username, String password) {
        HttpPost request = null;
        try {
            URI tdpLogin = new URIBuilder(tdpLoginUrl + "/login").build();

            request = new HttpPost(tdpLogin);
            request.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", username));
            postParameters.add(new BasicNameValuePair("password", password));

            request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

            HttpResponse response = httpClient.execute(request);

            return response.getHeaders(HttpHeaders.AUTHORIZATION)[0].getValue();

        } catch (URISyntaxException e) {
            e.printStackTrace(); // Logger
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (request != null) {
                request.releaseConnection();
            }
        }
        return null;
    }

    public Schema getSchema(String dataSetId, String bearer) {
        HttpResponse response = null;
        HttpGet request = null;
        try {
            URI datasetURI = new URIBuilder(dataInventoryUrl + "/api/v1/dataset-sample/" + dataSetId + "?method=cache&offset=0&limit=0&sample=head&withQuality=true&format=flatten")
                    .build();
            request = new HttpGet(datasetURI);
            request.addHeader(HttpHeaders.AUTHORIZATION, bearer);

            response = httpClient.execute(request);
            System.out.println("Status " + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 200) {
                String entity = IOUtils.toString(response.getEntity().getContent(), "UTF8");
                System.out.println(entity);

                Jsonb jsonb = JsonbBuilder.create();
                SchemaResponse schemaResponse = jsonb.fromJson(entity, SchemaResponse.class);

                return schemaResponse.getSchema();
            }
            else {
                // Log status code and answer
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace(); // Add a logger
        }
        finally {
            if (request != null) {
                request.releaseConnection();
            }
        }
        return null;

    }
}
