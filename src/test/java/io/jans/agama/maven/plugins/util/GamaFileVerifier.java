package io.jans.agama.maven.plugins.util;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GamaFileVerifier {
    
    private static final String  SHA_256_ALGORITHM = "SHA-256";
    private final MessageDigest md;

    public GamaFileVerifier()  throws NoSuchAlgorithmException {

        md = MessageDigest.getInstance(SHA_256_ALGORITHM);
    }

    public boolean fileInGamaSameAsLocalFile(final ZipFile gamafile, final String pathingama, final File localfile) throws Exception {

        final ZipEntry entry = gamafile.getEntry(pathingama);
        final InputStream zipstream = gamafile.getInputStream(entry);
        final String hash_of_file_in_zip = calculateDigest(zipstream);
        zipstream.close();
        
        final FileInputStream localfs = new FileInputStream(localfile);
        final String hash_of_local_file = calculateDigest(localfs);
        localfs.close();

        return hash_of_file_in_zip.equals(hash_of_local_file);
    }

    private String calculateDigest(final InputStream stream) throws Exception {


        byte [] buffer = new byte[1024];
        int bytecount = 0;
        md.reset();
        while((bytecount = stream.read(buffer,0,1024)) != -1 ) {
            md.update(buffer,0,bytecount);
        }

        byte [] digest = md.digest();
        final StringBuilder sb = new StringBuilder();
        for(int i = 0; i < digest.length; i++ ) {
            final String s = Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
            sb.append(s);
        }
        return sb.toString();
    }
}
