package io.jans.agama.maven.plugins;

import io.jans.agama.maven.plugins.PublishGamaException;
import io.jans.agama.maven.plugins.service.GamaPublisher;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;



@Mojo( name = "publish-gama", defaultPhase = LifecyclePhase.INSTALL )
public class PublishGamaMojo extends AbstractMojo {

    @Parameter( defaultValue="${project.build.directory}/${project.build.finalName}.gama", readonly = false, required = true )
    private File gamaFile;

    @Parameter( name="serverUrl", property="gama-server.url", required = true )
    private URL serverUrl;

    @Parameter( name="apiKey", property="gama-server.apikey", readonly = false, required = true )
    private String apiKey;

    @Parameter( defaultValue="${project.artifactId}", name="projectName", property="gama-project.name", readonly = false, required = true)
    private String projectName;

    private GamaPublisher publisher;

    @Override
    public void execute() throws MojoExecutionException {

        if(gamaFile == null || !gamaFile.exists() || !gamaFile.isFile()) {
            throw  PublishGamaException.gamaFileNotFound(gamaFile);
        }

        if(apiKey == null  || apiKey.length() == 0) {
            throw PublishGamaException.missingApiKey();
        }

        if(projectName == null || projectName.length() == 0) {
            throw PublishGamaException.missingProjectName();
        }
        getLog().info(String.format("Deploying project `%s` to %s", projectName, serverUrl.toString()));
        publisher.configure(serverUrl, apiKey);
        publisher.publishGamaFile(projectName, gamaFile);
        getLog().info(String.format("Agama project `%s` deployed successfully.",projectName));
    }

    @Inject
    public void setPublisher(GamaPublisher publisher) {

        this.publisher = publisher;
    }
}
