package io.jans.agama.maven.plugins;


import java.io.File;
import java.util.List;
import java.util.zip.ZipFile;

import io.jans.agama.maven.plugins.GamaGenerationException;
import io.jans.agama.maven.plugins.GenerateGamaMojo;
import io.jans.agama.maven.plugins.ProcessFlowsMojo;
import io.jans.agama.maven.plugins.model.GamaFileContentDescriptor;
import io.jans.agama.maven.plugins.util.GamaFileVerifier;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import static org.junit.Assert.*;

import org.junit.Test;

public class GenerateGamaMojoTest extends AbstractMojoTestCase {

    private static final String PROJECT_WITH_NO_FLOWS_DIR = "target/test-classes/generate-gama/no-flows-dir.xml";
    private static final String PROJECT_WITH_NO_WEB_ASSETS_DIR = "target/test-classes/generate-gama/no-web-assets-dir.xml";
    private static final String FULL_AGAMA_PROJECT = "target/test-classes/generate-gama/full-gama.xml";

    private GamaFileVerifier verifier;

    @Override
    protected void setUp() throws Exception {

        super.setUp();
        verifier = new GamaFileVerifier();
    }

    @Override
    protected void tearDown() throws Exception {

        super.tearDown();
    }

    

    @Test
    public void testGenerateGamaWithNoFlowsDir() throws Exception {

         final File pom  = getTestFile(PROJECT_WITH_NO_FLOWS_DIR);
         assertTrue(pom.exists() && pom.isFile());
         final GenerateGamaMojo mojo = (GenerateGamaMojo) lookupMojo("generate-gama",pom);

         final File flows = getFlowsDirectory(mojo);
         assertNotNull(flows);
         assertTrue(!flows.exists());
         assertTrue(getReadmeFile(mojo) == null);
         assertTrue(getLicenseFile(mojo) == null);
         assertNotNull(getWebAssetsDirectory(mojo));
         assertTrue(getAdditionalJarsDirectory(mojo) == null);

         GamaGenerationException e = assertThrows(GamaGenerationException.class,() -> {
            mojo.execute();
         });

         assertEquals(e.getMessage(),GamaGenerationException.invalidFlowsDirectory(flows).getMessage());
    }

    
    @Test
    public void testGenerateGamaWithNoWebAssetsDir() throws Exception {

        final File pom  = getTestFile(PROJECT_WITH_NO_WEB_ASSETS_DIR);
        assertTrue(pom.exists() && pom.isFile());
        final GenerateGamaMojo mojo = (GenerateGamaMojo) lookupMojo("generate-gama",pom);
        final File webassets = getWebAssetsDirectory(mojo);
        GamaGenerationException e = assertThrows(GamaGenerationException.class, () -> {
            mojo.execute();
        });
        assertEquals(e.getMessage(),GamaGenerationException.invalidWebAssetsDirectory(webassets).getMessage());
    }

    
    @Test
    public void testGenerateCompleteGama() throws Exception {

        final File pom = new File(FULL_AGAMA_PROJECT);
        assertTrue(pom.exists());
        final GenerateGamaMojo mojo = (GenerateGamaMojo) lookupMojo("generate-gama",pom);
        final File outputFile = getOutputFile(mojo);
        mojo.execute();
        assertTrue(outputFile.exists());
        final ZipFile gamafile = new ZipFile(outputFile);
        
        //verify file contents
        GamaFileContentDescriptor descriptor = new GamaFileContentDescriptor();
        final File readmeFile = getReadmeFile(mojo);
        descriptor.setReadme(getReadmeFile(mojo));
        descriptor.setLicense(getLicenseFile(mojo));
        descriptor.setProjectDescriptor(getProjectDescriptor(mojo));
        descriptor.setFlowsPath(getFlowsDirectory(mojo));
        descriptor.setWebPath(getWebAssetsDirectory(mojo));
        descriptor.setLibPath(getAdditionalJarsDirectory(mojo));
        
        assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile,"README.md",descriptor.getReadme()));
        assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile, "LICENSE", descriptor.getLicense()));
        assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile, "project.json", descriptor.getProjectDescriptor()));

        List<File> flows  = descriptor.getFlows();
        assertTrue(!flows.isEmpty());
        for(final File flow : flows) {
            final String flowpathingama = String.format("code/%s",flow.getName());
            assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile,flowpathingama,flow));
        }

        List<File> jars = descriptor.getJarFiles();
        assertTrue(!jars.isEmpty());
        for(final File jar : jars) {
            final String jarpathingama = String.format("lib/%s",jar.getName());
            assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile, jarpathingama,jar));
        }

        List<File> webassets = descriptor.getWebAssets();
        final File webassetsdir = getWebAssetsDirectory(mojo);
        assertTrue(!webassets.isEmpty());
        for(final File asset: webassets) {
            final String assetpathingama = webAssetPathInGama(webassetsdir, asset);
            assertTrue(verifier.fileInGamaSameAsLocalFile(gamafile,assetpathingama,asset));
        }
        gamafile.close();
        outputFile.delete();
    }

    private final String webAssetPathInGama(final File basepath, final File asset) {

        String relpath = asset.getAbsolutePath().substring(basepath.getAbsolutePath().length());

        if(relpath.charAt(0) == File.separatorChar) {
            relpath = relpath.substring(1);
        }
        return String.format("web/%s",relpath);
    }

    private final File getReadmeFile(final GenerateGamaMojo mojo) throws IllegalAccessException{

        return (File) getVariableValueFromObject(mojo,"readmeFile");
    }

    private final File getLicenseFile(final GenerateGamaMojo mojo) throws IllegalAccessException {

        return (File) getVariableValueFromObject(mojo,"licenseFile");
    }

    private final File getProjectDescriptor(final GenerateGamaMojo mojo) throws IllegalAccessException {

        return (File) getVariableValueFromObject(mojo,"projectDescriptor");
    }

    private final File getFlowsDirectory(final GenerateGamaMojo mojo) throws IllegalAccessException {

        return (File) getVariableValueFromObject(mojo,"flows");
    }

    private final File getWebAssetsDirectory(final GenerateGamaMojo mojo) throws IllegalAccessException {

        return (File) getVariableValueFromObject(mojo,"webAssets");
    }

    private final File getAdditionalJarsDirectory(final GenerateGamaMojo mojo) throws IllegalAccessException {
        
        return (File) getVariableValueFromObject(mojo,"additionalJars");
    }

    private final File getOutputFile(final GenerateGamaMojo mojo) throws IllegalAccessException {

        return (File) getVariableValueFromObject(mojo,"outputFile");
    }
}
