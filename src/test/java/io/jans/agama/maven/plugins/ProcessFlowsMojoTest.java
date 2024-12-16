package  io.jans.agama.maven.plugins;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import static org.junit.Assert.*;
import org.junit.Test;


public class ProcessFlowsMojoTest  extends AbstractMojoTestCase {
    
    private static final String PROJECT_WITH_NON_EXISTING_FLOWS_PATH  = "target/test-classes/process-flows/no-flows-path.xml";
    private static final String PROJECT_WITH_NON_DIRECTORY_FLOWS_PATH = "target/test-classes/process-flows/flows-path-not-dir.xml";
    private static final String PROJECT_WITH_NO_FLOWS = "target/test-classes/process-flows/no-flows.xml";
    private static final String PROJECT_WITH_VALID_FLOWS = "target/test-classes/process-flows/valid-flows.xml";

    @Test
    public void testInvalidFlowsPath() throws Exception {

        final File pom = getTestFile(PROJECT_WITH_NON_EXISTING_FLOWS_PATH);
        assertNotNull(pom);
        assertTrue(pom.exists());
        final ProcessFlowsMojo mojo = (ProcessFlowsMojo) lookupMojo("process-flows",pom);
        assertNotNull(mojo);
        final File flowsPath = (File) getVariableValueFromObject(mojo,"flowsPath");
        assertNotNull(flowsPath);
        assertTrue(!flowsPath.exists());
        ProcessFlowsException e = assertThrows(ProcessFlowsException.class, () -> {
            mojo.execute();
        });
        assertTrue(e.getMessage().equals(ProcessFlowsException.flowsPathDoesNotExist(flowsPath).getMessage()));
    }

    @Test
    public void testFlowsPathNotDirectory() throws Exception {

        final File pom = getTestFile(PROJECT_WITH_NON_DIRECTORY_FLOWS_PATH);
        assertNotNull(pom);
        assertTrue(pom.exists());
        final ProcessFlowsMojo mojo = (ProcessFlowsMojo) lookupMojo("process-flows",pom);
        assertNotNull(mojo);
        final File flowsPath = (File) getVariableValueFromObject(mojo,"flowsPath");
        assertNotNull(flowsPath);
        assertTrue(flowsPath.exists() && !flowsPath.isDirectory());
        ProcessFlowsException e = assertThrows(ProcessFlowsException.class, () -> {
            mojo.execute();
        });
        assertTrue(e.getMessage().equals(ProcessFlowsException.flowsPathIsNotDirectory(flowsPath).getMessage()));
    }
    
    
    @Test
    public void testFlowsPathHasNoFlows() throws Exception {

        final File pom = getTestFile(PROJECT_WITH_NO_FLOWS);
        assertNotNull(pom);
        assertTrue(pom.exists());
        final ProcessFlowsMojo mojo = (ProcessFlowsMojo) lookupMojo("process-flows",pom);
        assertNotNull(mojo);
        final File flowsPath = (File) getVariableValueFromObject(mojo,"flowsPath");
        assertNotNull(flowsPath);
        flowsPath.mkdir();
        flowsPath.deleteOnExit();
        assertTrue(flowsPath.isDirectory());
        assertTrue(flowsPath.listFiles() == null || flowsPath.listFiles().length == 0);
        mojo.execute();
        final File jsOutputDir = mojo.getJsOutputDirectory();
        if(jsOutputDir!= null && jsOutputDir.exists()) {
            assertTrue(jsOutputDir.listFiles() == null);
        }
    }

    
    @Test
    public void testFlowsPathHasValidFlows() throws Exception {

        final File pom = getTestFile(PROJECT_WITH_VALID_FLOWS);
        assertNotNull(pom);
        assertTrue(pom.exists());
        final ProcessFlowsMojo mojo = (ProcessFlowsMojo) lookupMojo("process-flows",pom);
        assertNotNull(mojo);
        final File flowsPath = (File) getVariableValueFromObject(mojo,"flowsPath");
        assertNotNull(flowsPath);
        mojo.execute();

        final File jsOutputDir = mojo.getJsOutputDirectory();
        assertNotNull(jsOutputDir);
        assertTrue(jsOutputDir.exists());
        final File [] generatedJsfiles = jsOutputDir.listFiles((f)-> { return f.getName().lastIndexOf(".js") != -1; });
        assertTrue(generatedJsfiles != null);
        final File [] flowFiles = flowsPath.listFiles((f) -> { return f.getName().lastIndexOf(".flow") != -1; });
        assertTrue(flowFiles.length == generatedJsfiles.length);
    }
}
