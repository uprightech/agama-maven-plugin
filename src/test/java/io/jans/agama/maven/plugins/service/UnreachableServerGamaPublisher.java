package io.jans.agama.maven.plugins.service;

import java.io.File;
import java.net.URL;

import javax.inject.Named;

import io.jans.agama.maven.plugins.PublishGamaException;

@Named("unreachable-server")
public class UnreachableServerGamaPublisher implements GamaPublisher {
    
    private URL serverUrl;
    private String apiKey;

    @Override
    public void configure(final URL serverUrl, final String apiKey) throws PublishGamaException{

        this.serverUrl = serverUrl;
        this.apiKey  = apiKey;
    }

    @Override
    public void publishGamaFile(final String projectname, final File gamaFile) throws PublishGamaException {
        throw PublishGamaException.serverError(serverUrl,null);
    }
}
