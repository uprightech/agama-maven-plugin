package io.jans.agama.maven.plugins;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;


public class GamaGenerationException extends MojoExecutionException {
    
    private GamaGenerationException(final String message) {
        super(message);
    }
    
    private GamaGenerationException(final String message, Throwable cause) {
        super(message,cause);
    }

    private static GamaGenerationException invalidDirectory(final String type, final File path) {
        
        if(path == null) {
            return new GamaGenerationException(String.format("Unspecified directory for %s ",type));
        }
        return new GamaGenerationException(String.format("The specified directory for %s `%s` is empty or doesn't exist.",type,path.getAbsolutePath()));
    }

    public static GamaGenerationException invalidFlowsDirectory(final File path) {

        return invalidDirectory("flows", path);
    }

    public static GamaGenerationException invalidWebAssetsDirectory(final File path) {

        return invalidDirectory("web assets", path);
    }

    public static GamaGenerationException unsupportedPackagingSpecified() {

        return new GamaGenerationException("Your maven project specified an unsupported packaging. The only packaging supported is 'jar'");
    }
}
