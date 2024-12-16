package io.jans.agama.maven.plugins.service;

import java.io.File;
import java.net.URL;

import io.jans.agama.maven.plugins.PublishGamaException;

public interface GamaPublisher {
    public void configure(final URL serverUrl, final String apiKey) throws PublishGamaException;
    public void publishGamaFile(final String projectname, final File gamaFile) throws PublishGamaException;
}
