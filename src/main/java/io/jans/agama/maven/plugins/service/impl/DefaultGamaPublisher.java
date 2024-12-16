package io.jans.agama.maven.plugins.service.impl;

import java.io.File;
import java.net.URL;

import javax.inject.Named;
import io.jans.agama.maven.plugins.service.GamaPublisher;
import io.jans.agama.maven.plugins.PublishGamaException;
import io.jans.api.AgamaDeploymentResult;
import io.jans.api.ApiError;
import io.jans.api.JansConfigApi;

@Named
public class DefaultGamaPublisher implements GamaPublisher {
    
    private JansConfigApi api;
    private URL serverUrl;

    public void configure(final URL serverUrl, final String apiKey) throws PublishGamaException {

        this.serverUrl = serverUrl;
        api = new JansConfigApi(serverUrl,apiKey);
    }

    public void publishGamaFile(final String projectname, final File gamaFile) throws PublishGamaException {

        try {
            AgamaDeploymentResult result = api.deployAgamaProject(projectname ,gamaFile);
            if(!result.deploymentInitiated()) {
                throw PublishGamaException.deploymentError(serverUrl, result.getMessage());
            }
        }catch(ApiError e) {
            throw PublishGamaException.deploymentError(serverUrl,e);
        }
    }
}
