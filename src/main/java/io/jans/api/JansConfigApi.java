package io.jans.api;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;

import java.text.MessageFormat;

import java.util.List;
import java.util.ArrayList;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpDelete;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.FileEntity;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicHeader;


import org.apache.hc.client5.http.protocol.HttpClientContext;

import org.json.JSONException;
import org.json.JSONObject;



public class JansConfigApi {
    
    private final URL url;
    private final String apiKey;
    private static final String GAMA_FILE_CONTENT_TYPE = "application/zip";

    public JansConfigApi(final URL url, final String apiKey) {

        this.url = url;
        this.apiKey = apiKey;
    }

    public AgamaDeploymentResult deployAgamaProject(final String projectname, final File gamafile) {

        try(CloseableHttpClient client = newHttpClient(apiKey);) {
            final URI uri = agamaProjectResourceUri(url,projectname);
            final HttpPost deploygamarequest = new HttpPost(uri);
            final FileEntity postdata = new FileEntity(gamafile,ContentType.create(GAMA_FILE_CONTENT_TYPE));
            deploygamarequest.setHeader(HttpHeaders.CONTENT_TYPE,GAMA_FILE_CONTENT_TYPE);
            deploygamarequest.setEntity(postdata);
            return client.execute(deploygamarequest,(response) -> {
                if(response.getCode() == HttpStatus.SC_ACCEPTED) {
                    final String msg = EntityUtils.toString(response.getEntity());
                    return AgamaDeploymentResult.deploymentPending(msg);
                }else {
                    return AgamaDeploymentResult.deploymentError(response.getCode(),response.getReasonPhrase());
                }
            });
        }catch(Exception e) {
            throw new ApiError("Agama project deployment failed",e);
        }
    }

    public AgamaDeploymentDescriptor getAgamaDeployment(final String projectname) {

        try (CloseableHttpClient client = newHttpClient(apiKey);)  {
            final URI uri = agamaProjectResourceUri(url, projectname);
            final HttpGet deployinforequest = new HttpGet(uri);
            return client.execute(deployinforequest, (response) -> {

                if(response.getCode() == HttpStatus.SC_OK) {
                    final String data = EntityUtils.toString(response.getEntity());
                    JSONObject json = new JSONObject(data);
                    final String id = json.getString("jansId");
                    final boolean active = json.getBoolean("jansActive");
                    return new AgamaDeploymentDescriptor(id,active);
                }else if(response.getCode() == HttpStatus.SC_NOT_FOUND || response.getCode() == HttpStatus.SC_NO_CONTENT) {
                    return null;
                }else {
                    throw new ApiError(MessageFormat.format("Error (Http {0})",response.getCode()));
                }
            });
        }catch(Exception e) {
            throw new ApiError("Could not get agama deployment information",e);
        }
    }

    public boolean undeployAgamaProject(final String projectname) {

        try (CloseableHttpClient client = newHttpClient(apiKey);) {
            final URI uri = agamaProjectResourceUri(url, projectname);
            final HttpDelete undeployrequest = new HttpDelete(uri);
            return client.execute(undeployrequest, (response) -> {
                if(response.getCode() == HttpStatus.SC_NO_CONTENT) {
                    return true;
                }
                return false;
            });
        }catch(Exception e) {
            throw new ApiError("Could not undeploy agama project",e);
        }
    }

    private CloseableHttpClient newHttpClient(final String apiKey) {

        List<Header> defaultheaders = new ArrayList<>();
        defaultheaders.add(new BasicHeader(HttpHeaders.AUTHORIZATION,String.format("Bearer %s",apiKey)));
        return HttpClients.custom()
            .setDefaultHeaders(defaultheaders)
            .build();
    }

    private URI agamaProjectResourceUri(final URL url, final String projectname) throws URISyntaxException {

        return new URI(String.format("%s/agama-deployment/%s",url.toString(),projectname));
    }

}
