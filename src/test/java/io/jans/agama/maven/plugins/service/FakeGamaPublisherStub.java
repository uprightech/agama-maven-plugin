package io.jans.agama.maven.plugins.service;

import java.io.File;
import java.net.URL;

import javax.inject.Named;

import io.jans.agama.maven.plugins.PublishGamaException;
import io.jans.agama.maven.plugins.service.GamaPublisher;

@Named("fake-stub")
public class FakeGamaPublisherStub implements GamaPublisher {
    
    private static String FAKE_API_KEY;
    private static URL FAKE_SERVER;
    private static String FAKE_PROJECT_NAME;
    static {
        try {
            FAKE_API_KEY = "e30d08ed-5468-4b5e-ab82-a1ef3abf5ebb";
            FAKE_PROJECT_NAME = "fake-test-project";
            FAKE_SERVER  = new URL("https://test-gama-server.local");
        }catch(Exception ignored) {

        }
    }

    private URL serverUrl;
    private String apiKey;
    private File lastPublishedGama;
    private String projectName;
    @Override
    public void configure(final URL serverUrl, final String apiKey) throws PublishGamaException {

        this.serverUrl = serverUrl;
        this.apiKey = apiKey;
    }

    @Override
    public void publishGamaFile(final String projectname, final File gamaFile) throws PublishGamaException {
        
        if(!FAKE_SERVER.toString().equals(serverUrl.toString())) {
            throw PublishGamaException.serverError(serverUrl,null);
        }

        if(!FAKE_API_KEY.equals(apiKey)) {
            throw PublishGamaException.serverError(serverUrl, null);
        }

        this.lastPublishedGama = gamaFile;
        projectName = projectname;
    }

    public boolean isFakeServer(final URL url) {

        return FAKE_SERVER.toString().equals(url.toString());
    }

    public boolean isFakeApiKey(final String apiKey) {

        return FAKE_API_KEY.equals(apiKey);
    }

    public boolean isLastPublishedGamaFile(final File gamaFile) {

        return lastPublishedGama == gamaFile;
    }

    public boolean isFakeProject(final String projectName) {

        return FAKE_PROJECT_NAME.equals(projectName);
    }
}
