package io.jans.agama.maven.plugins.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.List;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONException;
import org.json.JSONObject;


public class GamaFileWriter {
    
    private static final String GAMA_FILE_EXTENSION = ".gama";

    private static final String FLOW_PATH_TEMPLATE = "code/%s";
    private static final String LIB_PATH_TEMPLATE  = "lib/%s";
    private static final String WEB_PATH_TEMPLATE  = "web/%s";

    private class FileToZip {
        private File file;
        private String pathInZip;

        public FileToZip(final File file, final String pathInZip) {
            this.file = file;
            this.pathInZip = pathInZip;
        }

        public File getFile() {
            return this.file;
        }

        public String getPathInZip() {
            return this.pathInZip;
        }

        public boolean exists() {

            return this.file.exists();
        }
    }

    //.gama content
    private FileToZip licenseFile; 
    private FileToZip readmeFile;
    private FileToZip projectDescriptorFile;
    private List<FileToZip> flowFiles;
    private List<FileToZip> webAssetFiles;
    private List<FileToZip> libFiles;

    public GamaFileWriter() {

        this.licenseFile = null;
        this.readmeFile  = null;
        this.projectDescriptorFile = null;
        this.flowFiles   = new ArrayList<FileToZip>();
        this.webAssetFiles = new ArrayList<FileToZip>();
        this.libFiles = new ArrayList<FileToZip>();
    }

    public GamaFileWriter setLicense(final File file) {

        licenseFile =  new FileToZip(file,"LICENSE");
        return this;
    }

    public GamaFileWriter setReadMe(final File file) {

        readmeFile = new FileToZip(file,"README.md");
        return this;
    }

    public GamaFileWriter setProjectDescriptor(final File file) {

        projectDescriptorFile = new FileToZip(file,"project.json");
        return this;
    }

    public GamaFileWriter addFlow(final File file) {

        final String pathInZip = String.format(FLOW_PATH_TEMPLATE,file.getName());
        flowFiles.add(new FileToZip(file,pathInZip));
        return this;
    }

    public GamaFileWriter addLibrary(final File file) {

        final String pathInZip = String.format(LIB_PATH_TEMPLATE,file.getName());
        libFiles.add(new FileToZip(file,pathInZip));
        return this;
    }

    public GamaFileWriter addWebAsset(final File file, final String relativePath) {

        final String pathInZip = String.format(WEB_PATH_TEMPLATE,relativePath);
        webAssetFiles.add(new FileToZip(file,pathInZip));
        return this;
    }


    public void createGamaFile(File outputFile) {
        
        ensureThereAreFlows();
        ensureThereAreWebAssets();
        ensureProjectDescriptorIsValid();
        ensureOutputFileHasGamaExtension(outputFile);

        final ZipOutputStream zipstream = createZipOutputStream(outputFile);
        try {
            addReadmeToZip(zipstream);
            addLicenseToZip(zipstream);
            addProjectDescriptorToZip(zipstream);
            addFlowsToZip(zipstream);
            addLibrariesToZip(zipstream);
            addWebAssetsToZip(zipstream);
        }finally {
            try {zipstream.close(); } catch(IOException ignored) { }
        } 
    }

    private void ensureOutputFileHasGamaExtension(final File outputfile) {

        if(outputfile.getName().lastIndexOf(GAMA_FILE_EXTENSION ) == -1) {
            throw GamaFileWriterError.fileHasNoGamaExtension(outputfile);
        }
    } 

    private final void ensureThereAreFlows() {

        if(flowFiles.isEmpty()) {
            throw GamaFileWriterError.missingFlows();
        }
    }

    private final void ensureThereAreWebAssets() {

        if(webAssetFiles.isEmpty()) {
            throw GamaFileWriterError.missingWebAssets();
        }
    }

    private final void ensureProjectDescriptorIsValid() {

        if(projectDescriptorFile == null || !projectDescriptorFile.exists()) {
            
            return;
        }

        final String jsondata = readFileContents(projectDescriptorFile.getFile());
        try {
            JSONObject obj = new JSONObject(jsondata);
            obj.clear(); // for now just to avoid compile time warnings about unused local variables 
        }catch(JSONException e) {
            throw GamaFileWriterError.validationError("Invalid agama project descriptor",e);
        }
    }

    private void addReadmeToZip(final ZipOutputStream zipstream) {

        if(readmeFile != null) {
            addFileToZip(readmeFile.getPathInZip(),readmeFile.getFile(),zipstream);
        }
    }

    private void addLicenseToZip(final ZipOutputStream zipstream) {

        if(licenseFile != null) {
            addFileToZip(licenseFile.getPathInZip(),licenseFile.getFile(),zipstream);
        }
    }

    private void addProjectDescriptorToZip(final ZipOutputStream zipstream) {

        if(projectDescriptorFile != null) {
            addFileToZip(projectDescriptorFile.getPathInZip(),projectDescriptorFile.getFile(),zipstream);
        }
    }

    private void addFlowsToZip(final ZipOutputStream zipstream) {

        for(FileToZip flowfile: flowFiles ) {
            addFileToZip(flowfile.getPathInZip(),flowfile.getFile(),zipstream);
        }
    }

    private void addLibrariesToZip(final ZipOutputStream zipstream) {

        for(FileToZip libfile : libFiles) {
            addFileToZip(libfile.getPathInZip(),libfile.getFile(),zipstream);
        }
    }

    private void addWebAssetsToZip(final ZipOutputStream zipstream) {

        for(FileToZip webassetfile : webAssetFiles) {
            addFileToZip(webassetfile.getPathInZip(),webassetfile.getFile(),zipstream);
        }
    }

    private final String readFileContents(final File file) {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            final StringBuilder sb = new StringBuilder();
            byte [] buffer = new byte[1024];
            while((fis.read(buffer)) != -1) {
                sb.append(new String(buffer));
            }
            return sb.toString();
        }catch(IOException e) {
            throw GamaFileWriterError.fileIoError(file,"");
        }finally {
            if(fis != null) {
                try { fis.close();} catch (IOException ignored) {}
            }
        }
    }

    private final ZipOutputStream createZipOutputStream(final File file) {

        try {
            FileOutputStream os = new FileOutputStream(file);
            return new ZipOutputStream(os,StandardCharsets.UTF_8);
        }catch(FileNotFoundException e) {
            throw GamaFileWriterError.fileIoError(file,"Could not open zip file");
        }
    }

    private final void addFileToZip(final String pathInZip, final File file, final ZipOutputStream zipstream) {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            int read = 0;
            byte [] buffer = new byte[1024];
            zipstream.putNextEntry(new ZipEntry(pathInZip));
            while((read = fis.read(buffer)) != -1) {
                zipstream.write(buffer,0,read);
            }
            zipstream.closeEntry();
        }catch(IOException e) {
            throw GamaFileWriterError.fileIoError(file,"Could not add file to zip");
        }finally {
            if(fis != null) {
                try {fis.close();} catch(IOException ignored) {}
            }
        }
    }
}
