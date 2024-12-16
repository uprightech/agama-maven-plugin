package io.jans.agama.maven.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;

public class ProcessFlowsException extends MojoExecutionException {
    
    private ProcessFlowsException(String message) {
        super(message);
    }

    private ProcessFlowsException(String message, Throwable cause) {
        super(message,cause);
    }

    public static ProcessFlowsException flowsPathDoesNotExist(File flowsPath) {

        return new ProcessFlowsException(String.format("The specified path to the agama flows `%s` does not exist.",flowsPath.getPath()));
    }

    public static ProcessFlowsException flowsPathIsNotDirectory(File flowsPath) {
        return new ProcessFlowsException(String.format("The specified path to the agama flows `%s` is not a directory.", flowsPath.getPath()));
    }

    public static ProcessFlowsException flowParseError(File flowPath, Throwable cause) {
        return new ProcessFlowsException(String.format("Error parsing flow `%s`",flowPath.getName()),cause);
    }

    public static ProcessFlowsException flowParseError(File flowsPath, String errmsg) {
        return new ProcessFlowsException(String.format("Error parsing flow `%s`. %s",flowsPath.getName(),errmsg));
    }

    public static ProcessFlowsException fileWriteError(File fileToWrite, IOException e) {
        return new ProcessFlowsException(String.format("Writing to file `%s` failed",fileToWrite.getPath()),e);
    }

    public static ProcessFlowsException fileNotFoundError(FileNotFoundException e) {
        return new ProcessFlowsException("File not found or non-existing directory",e);
    }

    public static ProcessFlowsException generalIOError(IOException e) {
        return new ProcessFlowsException("A general I/O error occured",e);
    }
}
