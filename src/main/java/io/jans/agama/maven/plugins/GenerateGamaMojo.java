package io.jans.agama.maven.plugins;

import java.io.File;
import java.text.MessageFormat;

// maven imports
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.jans.agama.maven.plugins.GamaGenerationException;
import io.jans.agama.maven.plugins.model.GamaFileWriter;


@Mojo( name = "generate-gama", defaultPhase = LifecyclePhase.PACKAGE)
public class GenerateGamaMojo extends AbstractMojo {

    private static final String JAR_PACKAGING = "jar";

    @Parameter( defaultValue="${project.basedir}/README.md", readonly = false, required = false )
    private File readmeFile;

    @Parameter( defaultValue="${project.basedir}/LICENSE", readonly = false, required = false )
    private File licenseFile;

    @Parameter( defaultValue="${project.basedir}/src/main/resources/agama/project.json", readonly = false, required = false )
    private File projectDescriptor;

    @Parameter( defaultValue="${project.basedir}/src/main/resources/agama/flows", readonly = false, required = true )
    private File flows;

    @Parameter( defaultValue="${project.basedir}/src/main/resources/agama/web", readonly = false, required = true )
    private File webAssets;
    
    @Parameter( defaultValue="${project.build.directory}/deps", readonly = false, required = false )
    private File additionalJars;

    @Parameter( defaultValue="${project.build.directory}/${project.build.finalName}.gama", readonly = false , required = true )
    private File outputFile;


    @Parameter (property = "project", readonly = true)
    private MavenProject project;

    public void execute() throws MojoExecutionException {
        
        final GamaFileWriter writer = new GamaFileWriter();
        if(project != null && !JAR_PACKAGING.equals(project.getPackaging())) {
            throw GamaGenerationException.unsupportedPackagingSpecified();
        }

        getLog().info("Verifying configuration before gama generation.");
        ensureFlowsDirectoryExists();
        ensureWebAssetsDirectoryExists();

        getLog().info("Processing optional files");
        addOptionalFiles(writer);
        getLog().info("Processing flows");
        addFlows(writer);
        getLog().info("Processing jar files");
        addJars(writer);
        getLog().info("Processing web assets");
        addWebAssets(webAssets,webAssets,writer);
        getLog().info(String.format("Generating gama file: %s",outputFile.getAbsolutePath()));
        writer.createGamaFile(outputFile);
    }

    private void ensureFlowsDirectoryExists() throws GamaGenerationException {

        if(flows == null) {
            throw GamaGenerationException.invalidFlowsDirectory(flows);
        }

        if(flows.exists() && flows.isDirectory()) {
            return;
        }
        throw GamaGenerationException.invalidFlowsDirectory(flows);
    }

    private void ensureWebAssetsDirectoryExists() throws GamaGenerationException {

        if(webAssets == null) {
            throw GamaGenerationException.invalidWebAssetsDirectory(webAssets);
        }
        if(webAssets.exists() && webAssets.isDirectory()) {
            return;
        }
        throw GamaGenerationException.invalidWebAssetsDirectory(webAssets);
    }

    private void addOptionalFiles(final GamaFileWriter writer) {

        if(readmeFile != null && readmeFile.exists() && readmeFile.isFile()) {
            getLog().debug(MessageFormat.format("Processing README file {0}",readmeFile.getAbsolutePath()));
            writer.setReadMe(licenseFile);
        }

        if(licenseFile != null && licenseFile.exists() && licenseFile.isFile()) {
            getLog().debug(MessageFormat.format("Processing LICENSE file {0}",licenseFile.getAbsolutePath()));
            writer.setLicense(licenseFile);
        }

        if(projectDescriptor != null && projectDescriptor.exists() && projectDescriptor.isFile()) {
            getLog().debug(MessageFormat.format("Processing project descriptor {0}",projectDescriptor.getAbsolutePath()));
            writer.setProjectDescriptor(projectDescriptor);
        }
    }

    private void addFlows(final GamaFileWriter writer) {

        File [] flowfiles = flows.listFiles((f)-> {
            return f.isFile() && (f.getName().lastIndexOf(".flow") != -1);
        });

        if(flowfiles == null) {
            return;
        }

        for(final File flow: flowfiles) {
            getLog().debug(MessageFormat.format("Processing flow file {0}",flow.getAbsolutePath()));
            writer.addFlow(flow);
        }
    }

    private void addJars(final GamaFileWriter writer)  {

        
        File [] jarfiles = additionalJars.listFiles( (f) -> {
            return f.isFile() && (f.getName().lastIndexOf(".jar") != -1);
        });

        if(jarfiles == null) {
            return;
        }

        for(final File jar : jarfiles) {
            getLog().debug(MessageFormat.format("Processing jar file {0}",jar.getAbsolutePath()));
            writer.addLibrary(jar);
        }
    }

    private void addWebAssets(final File rootdir, final File currentdir , final GamaFileWriter writer) {

        final String root_dir_path = rootdir.getAbsolutePath();
        File [] entries = currentdir.listFiles();
        if(entries == null) {
            return;
        }

        for(File entry : entries) {
            if(entry.isFile())  {
                String relpath = entry.getAbsolutePath().substring(root_dir_path.length());
                if(relpath.charAt(0) == File.separatorChar) {
                    relpath =  relpath.substring(1);
                }
                getLog().debug(MessageFormat.format("Processing web asset {0}",entry.getAbsolutePath()));
                writer.addWebAsset(entry, relpath);
            }else {
                addWebAssets(rootdir,entry, writer);
            }
        }
    }
}
