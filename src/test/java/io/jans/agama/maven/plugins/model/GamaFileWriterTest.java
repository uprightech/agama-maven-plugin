package io.jans.agama.maven.plugins.model;

import io.jans.agama.maven.plugins.model.GamaFileWriter;
import io.jans.agama.maven.plugins.model.GamaFileWriterError;
import io.jans.agama.maven.plugins.util.GamaFileVerifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.WithoutMojo;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class GamaFileWriterTest  {


    private static final String TEMP_OUTPUT_DIR = "target/generated-gama-files/";

    private final File licenseFile = new File("target/test-classes/generate-gama/exploded-gama/LICENSE");
    private final File readmeFile  = new File("target/test-classes/generate-gama/exploded-gama/README.md");
    private final File validProjectDescriptorFile =  new File("target/test-classes/generate-gama/exploded-gama/project.json");
    private final File invalidProjectDescriptorFile = new File("target/test-classes/generate-gama/exploded-gama/bad-project-descriptor.json");
    private final File exampleTemplateFile =  new File("target/test-classes/generate-gama/exploded-gama/web/example-template.ftlh");
    private final File utilJsFile = new File("target/test-classes/generate-gama/exploded-gama/web/js/util.js");
    private final File helloFlowFile = new File("target/test-classes/generate-gama/exploded-gama/code/io.jans.hello.flow");
    private final File goodbyeFlowFile = new File("target/test-classes/generate-gama/exploded-gama/code/io.jans.goodbye.flow");

    private GamaFileVerifier verifier;

    @Before
    public void setUp() throws NoSuchAlgorithmException {

        new File(TEMP_OUTPUT_DIR).mkdirs();
        verifier = new GamaFileVerifier();
    }

    @Test
    public void testFailToCreateEmptyGamaFile() {

        final GamaFileWriter writer = new GamaFileWriter();
        final File outputFile = tempFilePath("target/empty-gama-file.gama");
        outputFile.deleteOnExit();
        assertThrows(GamaFileWriterError.class,()-> {writer.createGamaFile(outputFile);});
    }

    @Test
    public void testFailToCreateGamaFileWithNoFlows() {

        final GamaFileWriter writer = new GamaFileWriter(); 
        final File outputFile = tempFilePath("gama-file-with-no-flows.gama");
        outputFile.deleteOnExit();
        assertThrows(GamaFileWriterError.class,()-> {
            writer.setLicense(licenseFile);
            writer.setReadMe(readmeFile);
            writer.setProjectDescriptor(validProjectDescriptorFile);
            writer.createGamaFile(outputFile);
        });
    }

    @Test
    public void testFailToCreateGamaFileWithNoWebAssets() {

        final GamaFileWriter writer = new GamaFileWriter();
        final File outputFile = tempFilePath("gama-file-with-no-web-assets.gama");
        outputFile.deleteOnExit();
        assertThrows(GamaFileWriterError.class,()-> {
            writer.setLicense(licenseFile);
            writer.setReadMe(readmeFile);
            writer.setProjectDescriptor(validProjectDescriptorFile);
            writer.addFlow(helloFlowFile);
            writer.createGamaFile(outputFile);
        });
    }

    @Test
    public void testFailToCreateGamaFileWithWrongExtention() {

        final GamaFileWriter writer = new GamaFileWriter();
        final File outputFile = tempFilePath("gama-file-with-wrong-extension.zip");
        outputFile.deleteOnExit();
        assertThrows(GamaFileWriterError.class, () -> {
            writer.setLicense(licenseFile);
            writer.setReadMe(readmeFile);
            writer.setProjectDescriptor(validProjectDescriptorFile);
            writer.addFlow(helloFlowFile);
            writer.addWebAsset(exampleTemplateFile,"example-template.ftlh");
            writer.createGamaFile(outputFile); 
        });
    }

    @Test
    public void testCreateValidGamaFile() throws Exception {

        final File outputFile = tempFilePath("valid-gama-file.gama");
        ZipFile gamafile = null;
        try {
            final GamaFileWriter writer = new GamaFileWriter();
            writer.setLicense(licenseFile);
            writer.setReadMe(readmeFile);
            writer.setProjectDescriptor(validProjectDescriptorFile);
            writer.addFlow(helloFlowFile);
            writer.addWebAsset(exampleTemplateFile,"example-template.ftlh");
            writer.createGamaFile(outputFile);
            assertTrue(outputFile.exists());
            gamafile = new ZipFile(outputFile);
            assertTrue(gamafile.size() != 0);
        }finally {
            outputFile.delete();
            if(gamafile != null) {
                try { gamafile.close(); } catch(IOException ignored) {}
            }
        }
    }

    @Test
    public void testCreatedGamaFileHasAllSpecifiedFiles() throws Exception {

        final File outputFile  = tempFilePath("example-project.gama");
        ZipFile gamafile = null;
        try {
            final GamaFileWriter writer = new GamaFileWriter();
            writer.setLicense(licenseFile);
            writer.setReadMe(readmeFile);
            writer.setProjectDescriptor(validProjectDescriptorFile);

            writer.addFlow(helloFlowFile);
            writer.addFlow(goodbyeFlowFile);

            writer.addWebAsset(exampleTemplateFile,"example-template.ftlh");
            writer.addWebAsset(utilJsFile,"js/util.js");

            writer.createGamaFile(outputFile);
            assertTrue(outputFile.exists());

            gamafile = new ZipFile(outputFile);
            assertTrue(gamafile.size() != 0);

            assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile,"LICENSE",licenseFile));
            assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile,"README.md",readmeFile));
            assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile,"project.json",validProjectDescriptorFile));

            assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile, "code/io.jans.hello.flow",helloFlowFile));
            assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile, "code/io.jans.goodbye.flow",goodbyeFlowFile));

            assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile, "web/example-template.ftlh", exampleTemplateFile));
            assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile, "web/js/util.js", utilJsFile));
            
        }finally {
            if(gamafile != null) {
                try {gamafile.close();} catch(IOException ignored) {}
            }
            outputFile.delete();
        }
    }

    private File tempFilePath(final String relativePath) {

        File ret = new File(TEMP_OUTPUT_DIR + relativePath);
        ret.deleteOnExit();
        return ret;
    }

}

    
