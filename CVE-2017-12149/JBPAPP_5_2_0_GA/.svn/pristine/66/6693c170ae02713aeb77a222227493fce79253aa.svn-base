package org.jboss.check;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.jar.JarFile;
import java.util.Map;
import java.util.zip.ZipException;
import java.util.zip.ZipEntry;

public class JARCheck {

    static String jboss_home;

    public static void main(String [] args) {
        if (args.length == 0) {
            System.err.println("Please specify EAP's root directory as a command line argument.");
            return;
        }

        try {
            File root = new File(args[0]);
            jboss_home = root.getCanonicalPath();
            if (!root.exists()) {
                System.err.println(jboss_home + " does not exist.");
                return;
            }
            walk(root);
        }
        catch (IOException ioe) {
            System.err.println(ioe);
        }
        catch (NoSuchAlgorithmException nsae) {
            System.err.println(nsae.toString());
        }
    }

    public static void walk(File root) throws NoSuchAlgorithmException {
        for(File f : root.listFiles()) {
            if(f.isDirectory()) {
                walk(f);
            }
            else {
                check(f);
            }
        }
    }

    public static String getManifestString(JarFile jf) throws java.io.IOException {
        StringBuilder buffer = new StringBuilder();
        try {
            ZipEntry manifest = jf.getEntry("META-INF/MANIFEST.MF");
            InputStream manifest_is = jf.getInputStream(manifest);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(manifest_is));
            String line = reader.readLine();
            while( line.length() != 0 ) {
                buffer.append(line + "\n");
                line = reader.readLine();
            }
            return buffer.toString();
        } catch (NullPointerException npe) {
            // No MANIFEST
            return "";
        }
    }

    public static void check(File f) throws NoSuchAlgorithmException {
        try {
            MessageDigest alg = MessageDigest.getInstance("md5");
            JarFile jf = new JarFile(f);
            FileInputStream fis = new FileInputStream(f);
            byte [] buffer = new byte[(int) f.length()];
            fis.read(buffer);
            alg.update(buffer);
            String sum = new BigInteger(1, alg.digest()).toString(16);
            System.out.println(
                    f.getCanonicalPath().replace(jboss_home, "JBOSSHOME") + "\n"
                    + sum + "\n"
                    + getManifestString(jf) + "===");
        }
        catch( java.util.zip.ZipException ze ) {
            // skip
        }
        catch( java.io.FileNotFoundException fnfe ) {
            System.err.println(fnfe.toString());
        }
        catch( java.io.IOException ioe ) {
            System.err.println(ioe.toString());
        }
    }
}
