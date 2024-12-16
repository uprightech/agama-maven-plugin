package io.jans.agama.maven.plugins;

import java.io.File;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;

public class PublishGamaException extends MojoExecutionException {
    
    private  PublishGamaException(final String message) {
        super(message);
    }

    private PublishGamaException(final String message, Throwable cause) {
        super(message,cause);
    }

    public static PublishGamaException gamaFileNotFound(final File gamafile) {

        if(gamafile == null) {
            return new PublishGamaException("No gama file at the specified path");
        }else {
            return new PublishGamaException(String.format("The gama file `%s` was not found",gamafile.getAbsolutePath()));
        }
    }

    public static PublishGamaException serverError(final URL serverUrl, Throwable cause) {

        return new PublishGamaException(String.format("An error occured contacting server '%s'",serverUrl),cause);
    }

    public static PublishGamaException missingApiKey() {

        return new PublishGamaException("Missing API Key");
    }

    public static PublishGamaException missingProjectName() {

        return new PublishGamaException("Missing project name");
    }

    public static PublishGamaException deploymentError(final URL serverUrl, final Throwable cause) {

        return new PublishGamaException(String.format("Error deploying gama file on server '%s'.",serverUrl),cause);
    }

    public static PublishGamaException deploymentError(final URL serverUrl, final String message) {

        return new PublishGamaException(String.format("Error deploying gama file on server '%s'. %s",serverUrl,message));
    }
}
