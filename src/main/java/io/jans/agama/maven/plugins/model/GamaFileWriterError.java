package io.jans.agama.maven.plugins.model;

import java.io.File;
import java.io.IOException;

public class GamaFileWriterError extends RuntimeException{
    
    public GamaFileWriterError(final String msg) {
        super(msg);
    }

    public GamaFileWriterError(final String msg, Throwable cause) {
        super(msg,cause);
    }

    public static GamaFileWriterError createError(final String msg, final Throwable cause) {
        return new GamaFileWriterError(msg,cause);
    }

    public static GamaFileWriterError fileHasNoGamaExtension(final File file) {

        return new  GamaFileWriterError(String.format("The specified file path `%s` does not have a gama extension",file.getAbsolutePath()));
    }
    
    public static GamaFileWriterError invalidFilePath(final File file) {

        return new GamaFileWriterError(String.format("The specified file `%s` does not exist",file.getAbsolutePath()));
    }

    public static GamaFileWriterError fileIoError(final File file, IOException e) {
        return new GamaFileWriterError(String.format("I/O error processing file `%s`",file.getAbsolutePath()),e);
    }

    public static GamaFileWriterError fileIoError(final File file , final String message) {
        return new GamaFileWriterError(String.format("I/O error processing file `%s`. %s",file.getAbsolutePath(),message));
    }

    public static GamaFileWriterError missingFlows() {

        return new GamaFileWriterError("At least one flow file must be specified.");
    }

    public static GamaFileWriterError missingWebAssets() {
        
        return new GamaFileWriterError("At least one web asset must be specified.");
    }

    public static GamaFileWriterError fileToZipNotFound(final File file, final String type) {

        return new GamaFileWriterError(String.format("The file `%s` of type `%s` was not found",file.getAbsolutePath(),type));
    }

    public static GamaFileWriterError invalidProjectDescriptor(final File file) {
        return new GamaFileWriterError("The specified project descriptor is invalid.");
    }

    public static GamaFileWriterError validationError(final String msg, Throwable cause) {

        return new GamaFileWriterError(msg,cause);
    }
 }
