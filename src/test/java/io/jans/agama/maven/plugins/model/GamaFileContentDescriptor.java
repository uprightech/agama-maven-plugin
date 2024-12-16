package io.jans.agama.maven.plugins.model;

import java.io.File;
import java.io.FileFilter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class GamaFileContentDescriptor {
    
    private File license;
    private File readme;
    private File projectDescriptor;
    private File flowsPath;
    private File libPath;
    private File webPath;
    
    public GamaFileContentDescriptor setLicense(final File license)  {

        this.license = license;
        return this;
    }

    public File getLicense() {

        return license;
    }

    public GamaFileContentDescriptor setReadme(final File readme) {

        this.readme = readme;
        return this;
    }

    public File getReadme() {

        return readme;
    }

    public GamaFileContentDescriptor setProjectDescriptor(final File projectDescriptor) {

        this.projectDescriptor = projectDescriptor;
        return this;
    }

    public File getProjectDescriptor() {

        return projectDescriptor;
    }

    public GamaFileContentDescriptor setFlowsPath(final File flowsPath) {

        this.flowsPath = flowsPath;
        return this;
    }

    public List<File> getFlows()  {

        if(flowsPath == null ) {
            return new ArrayList<>();
        }
        
        final File [] ret = flowsPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                final String ext  = ".flow";
                final String name = pathname.getName();
                return (name.lastIndexOf(ext) + new String(ext).length()) == (name.length());
            }
        });

        if(ret == null) {
            return new ArrayList<>();
        }

        return Arrays.asList(ret);
    }

    
    public GamaFileContentDescriptor setLibPath(final File libPath) {
    
        this.libPath = libPath;
        return this;
    }

    public List<File> getJarFiles()  {

        if(libPath == null) {
            return new ArrayList<>();
        }

        final File [] ret =  libPath.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File pathname) {
                final String ext = ".jar";
                final String name = pathname.getName();
                return (name.lastIndexOf(ext) + new String(ext).length()) == (name.length());
            }
        });

        return Arrays.asList(ret);
    }
    
    public GamaFileContentDescriptor setWebPath(final File webPath) {

        this.webPath = webPath;
        return this;
    }

    public List<File> getWebAssets() {


        if(webPath == null) {
            return new ArrayList<>();
        }

        final File [] dirlist = webPath.listFiles();
        List<File> ret = new ArrayList<>();
        for(final File file : dirlist) {
            if(file.isFile()) {
                ret.add(file);
            }else if(file.isDirectory()) {
                getWebAssetsFromDirectory(file,ret);
            }
        }
        return ret;
    }

    private void getWebAssetsFromDirectory(final File directory, List<File> webassets){

        for(final File file: directory.listFiles()) {
            if(file.isFile()) {
                webassets.add(file);
            }else if(file.isDirectory()) {
                getWebAssetsFromDirectory(file, webassets);
            }
        }
    }
}
