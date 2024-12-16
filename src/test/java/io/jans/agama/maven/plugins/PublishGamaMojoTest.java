package io.jans.agama.maven.plugins;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import io.jans.agama.maven.plugins.service.FakeGamaPublisherStub;
import io.jans.agama.maven.plugins.service.GamaPublisher;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Test;


public class PublishGamaMojoTest extends AbstractMojoTestCase {


    private static final String PROJECT_WITH_NO_GAMA = "target/test-classes/publish-gama/no-gama.xml";
    private static final String PROJECT_WITH_UNREACHABLE_SERVER = "target/test-classes/publish-gama/unreachable-server.xml";
    private static final String PROJECT_WITH_MISSING_API_KEY = "target/test-classes/publish-gama/missing-api-key.xml";
    private static final String PROJECT_WITH_FAKE_OK_SERVER = "target/test-classes/publish-gama/fake-ok-server.xml";

    @Test
    public void testPublishNonExistingGamaFile() throws Exception {

        final File pom = getTestFile(PROJECT_WITH_NO_GAMA);
        assertNotNull(pom);
        assertTrue(pom.exists());
        final PublishGamaMojo mojo = (PublishGamaMojo) lookupMojo("publish-gama",pom);
        assertNotNull(mojo);
        final File gamaFile = (File) getVariableValueFromObject(mojo,"gamaFile");
        assertNotNull(gamaFile);
        assertTrue(!gamaFile.exists());
        GamaPublisher pub = getGamaPublisher("dummy-publisher");
        assertNotNull(pub);
        mojo.setPublisher(pub);
        final PublishGamaException e = assertThrows(PublishGamaException.class, () -> {
           mojo.execute();
        });
        assertEquals(PublishGamaException.gamaFileNotFound(gamaFile).getMessage(),e.getMessage());
    }

    @Test
    public void testPublishGamaFileWithMissingApiKey() throws Exception {

        final File pom = getTestFile(PROJECT_WITH_MISSING_API_KEY);
        assertNotNull(pom);
        assertTrue(pom.exists());
        final PublishGamaMojo mojo = (PublishGamaMojo) lookupMojo("publish-gama",pom);
        assertNotNull(mojo);
        final File gamaFile = (File) getVariableValueFromObject(mojo, "gamaFile");
        assertNotNull(gamaFile);
        assertTrue(gamaFile.exists());
        final URL serverUrl = (URL) getVariableValueFromObject(mojo,"serverUrl");
        assertNotNull(serverUrl);
        final String apiKey = (String) getVariableValueFromObject(mojo,"apiKey");
        assertTrue(apiKey == null);

        GamaPublisher pub = getGamaPublisher("dummy-publisher");
        assertNotNull(pub);
        mojo.setPublisher(pub);
        final PublishGamaException e = assertThrows(PublishGamaException.class, () -> {
            mojo.execute();
        });

        assertEquals(PublishGamaException.missingApiKey().getMessage(),e.getMessage());
    }
    
    @Test
    public void testPublishGamaFileToUnreachableServer() throws Exception {

        final File pom = getTestFile(PROJECT_WITH_UNREACHABLE_SERVER);
        assertNotNull(pom);
        assertTrue(pom.exists());
        final PublishGamaMojo mojo = (PublishGamaMojo) lookupMojo("publish-gama",pom);
        assertNotNull(mojo);

        final File gamaFile = (File) getVariableValueFromObject(mojo,"gamaFile");
        assertNotNull(gamaFile);
        assertTrue(gamaFile.exists());
        final URL serverUrl = (URL) getVariableValueFromObject(mojo,"serverUrl"); 
        assertNotNull(serverUrl);
        final String apiKey = (String) getVariableValueFromObject(mojo,"apiKey");
        assertNotNull(apiKey);

        GamaPublisher pub = getGamaPublisher("unreachable-server");
        assertNotNull(pub);
        mojo.setPublisher(pub);

        final PublishGamaException e = assertThrows(PublishGamaException.class, () -> {
            mojo.execute();
        });
        assertEquals(PublishGamaException.serverError(serverUrl,null).getMessage(),e.getMessage());
    }

     
    @Test
    public void testSuccessfullyPublishGamaFile() throws Exception {

        final File pom = getTestFile(PROJECT_WITH_FAKE_OK_SERVER);
        assertNotNull(pom);
        assertTrue(pom.exists());
        final PublishGamaMojo mojo = (PublishGamaMojo) lookupMojo("publish-gama",pom);
        assertNotNull(mojo);

        final File gamaFile = (File) getVariableValueFromObject(mojo,"gamaFile");
        assertNotNull(gamaFile);
        assertTrue(gamaFile.exists());

        final URL serverUrl =(URL) getVariableValueFromObject(mojo,"serverUrl");
        assertNotNull(serverUrl);

        final String apiKey = (String) getVariableValueFromObject(mojo,"apiKey");
        assertNotNull(apiKey);

        final String projectName = (String) getVariableValueFromObject(mojo,"projectName");
        assertNotNull(projectName);

        FakeGamaPublisherStub pub = (FakeGamaPublisherStub) getGamaPublisher("fake-stub");
        assertNotNull(pub);
        assertTrue(pub.isFakeApiKey(apiKey));
        assertTrue(pub.isFakeServer(serverUrl));
        assertTrue(pub.isFakeProject(projectName));
        mojo.setPublisher(pub);
        mojo.execute();
        assertTrue(pub.isLastPublishedGamaFile(gamaFile));
    } 

    private GamaPublisher getGamaPublisher(final String rolehint) throws Exception {

        return (GamaPublisher) getContainer().lookup(GamaPublisher.class,rolehint);
    }
}
