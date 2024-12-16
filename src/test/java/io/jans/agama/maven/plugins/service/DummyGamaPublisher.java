package io.jans.agama.maven.plugins.service;

import java.io.File;
import java.net.URL;

import javax.inject.Named;

import io.jans.agama.maven.plugins.PublishGamaException;

@Named("dummy-publisher")
public class DummyGamaPublisher implements GamaPublisher {
    
    @Override
    public void configure(final URL serverUrl, final String apiKey) throws PublishGamaException {

    }

    @Override
    public void publishGamaFile(final String projectname, final File gamaFile) throws PublishGamaException {

    }
}
