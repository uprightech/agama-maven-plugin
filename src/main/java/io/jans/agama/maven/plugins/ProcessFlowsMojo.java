package io.jans.agama.maven.plugins;

// agama transpiler imports 
import io.jans.agama.dsl.*;
import io.jans.agama.dsl.error.SyntaxException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


@Mojo( name = "process-flows", defaultPhase = LifecyclePhase.COMPILE )
public class ProcessFlowsMojo extends AbstractMojo {
    
    
    private final String GENERATED_JS_OUTPUT_DIRNAME = "js";

    private final FileFilter flowsFilter = new FileFilter() {
        
        @Override
        public boolean accept(File path) {

            final String name = path.getName();
            final int dotindex = name.lastIndexOf(".");
            if(dotindex > 0) {
                final String ext = name.substring(dotindex+1);
                return "flow".equalsIgnoreCase(ext);
            }
            return false;
        }
    };

    /**
     * Path to directory containing the agama flows (files with .flow extension)
     */
    @Parameter( defaultValue = "${project.basedir}/src/main/resources/agama/flows", readonly = false, required = true )
    private File flowsPath;

    /**
     * Output directory for generated assets 
     */
    @Parameter( defaultValue = "${project.build.directory}/agama/", readonly = false, required = true )
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {

        if(!flowsPath.exists()) {

            final ProcessFlowsException e = ProcessFlowsException.flowsPathDoesNotExist(flowsPath);
            getLog().error(e.getMessage());
            throw e;
        }

        if(!flowsPath.isDirectory()) {

            final ProcessFlowsException e = ProcessFlowsException.flowsPathIsNotDirectory(flowsPath);
            getLog().error(e.getMessage());
            throw e;
        }

        final File [] flows  = flowsPath.listFiles(flowsFilter);
        if(flows.length == 0) {
            getLog().warn(String.format("No flows found in specified directory `%s`",flowsPath.getPath()));
            return;
        }

        final File jsOutputDirectory = createJsOutputDirectory();

        for(final File flow: flows) {
            getLog().info(String.format("Processing agama flow %s ...",flow.getName()));
            TranspilationResult res = transpileFlow(flow);
            final File outputJsFile = saveGeneratedJs(res.getCode(),jsOutputDirectory,flow);
        }
    }

    public final File getJsOutputDirectory() {

        if(outputDirectory == null) {
            return null;
        }
        
        final String flowsDirPath = outputDirectory.getAbsolutePath()+ "/" + GENERATED_JS_OUTPUT_DIRNAME;
        return new File(flowsDirPath);
    }

    private final File saveGeneratedJs(final String generatedJs, final File jsOutputDirectory, final File flow) throws ProcessFlowsException {

        FileOutputStream fs = null;
        File outputJsFile = null; 
        try {
            outputJsFile = outputJsFileForFlow(jsOutputDirectory,flow);
            fs = new FileOutputStream(outputJsFile);
            fs.write(generatedJs.getBytes("UTF-8"));
            return outputJsFile;
        }catch (FileNotFoundException e) {
            throw ProcessFlowsException.fileNotFoundError(e);
        }catch(IOException e) {
            throw ProcessFlowsException.fileWriteError(outputJsFile,e);
        }finally {
            try{
                if(fs != null) {
                    fs.close();
                }
            }catch(IOException e) {
                throw ProcessFlowsException.generalIOError(e);
            }
        }
    }

    private final File createJsOutputDirectory() {

        final String flowsDirPath = outputDirectory.getAbsolutePath()+ "/" + GENERATED_JS_OUTPUT_DIRNAME;
        final File flowsOutputDirectory =  new File(flowsDirPath);
        if(flowsOutputDirectory.exists()) {
            deleteDirectory(flowsOutputDirectory);
        }
        flowsOutputDirectory.mkdirs();
        return flowsOutputDirectory;
    }

    private final void deleteDirectory(final File directory) {

        final File [] dircontents = directory.listFiles();
        if(dircontents != null) {
            for(final File dirfile : dircontents) {
                deleteDirectory(dirfile);
            }
        }
        directory.delete();
    }


    private final File outputJsFileForFlow(final File jsOutputDirectory, final File flow) {
        
        final File jsFileName = new File(flowQNameFromFileName(flow.getName()) + ".js");
        return new File(jsOutputDirectory.getAbsolutePath(),jsFileName.getName());
    }

    
    private final String flowQNameFromFileName(final String fileName) {

        final int extensionpos = fileName.indexOf(".flow");
        if(extensionpos == -1) {
            return null;
        }
        return fileName.substring(0,extensionpos);
    }

    private final String flowSourceFromFile(final File flow) {

        try {
            return Files.readString(Paths.get(flow.getAbsolutePath()));
        }catch(IOException e) {
            return null;
        }
    }

    private final TranspilationResult transpileFlow(final File flow) throws ProcessFlowsException {

        try {
            final String flowQname  = flowQNameFromFileName(flow.getName());
            if(flowQname == null) {
                throw ProcessFlowsException.flowParseError(flow,"Could not obtain flow qualified name");
            }

            final String flowSource = flowSourceFromFile(flow);
            if(flowSource == null) {
                throw ProcessFlowsException.flowParseError(flow,"Could not load flow from file");
            }
            return Transpiler.transpile(flowQname,flowSource);
        }catch(TranspilerException e) {
            throw ProcessFlowsException.flowParseError(flow,e);
        }catch(SyntaxException e) {
            throw ProcessFlowsException.flowParseError(flow,e);
        }
    }
}
