package com.project944.cov.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropsUtils {
    
    public final static String mainWindowWidth = "mainWindowWidth";
    public final static String mainWindowHeight = "mainWindowHeight";
    public final static String iconSize = "iconSize";
    public final static String serverHost = "serverHost";
    public final static String playerName = "playerName";
    
    private Properties props = new Properties();
    
    public PropsUtils() {
        setDefaults();
        load();
    }
    
    private void setDefaults() {
        props.setProperty(mainWindowWidth, "600");
        props.setProperty(mainWindowHeight, "400");
        props.setProperty(iconSize, "48");
        props.setProperty(serverHost, "127.0.0.1");
        props.setProperty(playerName, "*");
    }
    
    public int getInt(String propName) {
        return Integer.parseInt(props.getProperty(propName));
    }
    public void setInt(String propName, int value) {
        props.setProperty(propName, ""+value);
    }
    public String getString(String propName) {
        return props.getProperty(propName);
    }
    public void setString(String propName, String value) {
        props.setProperty(propName, value);
    }
    
    private String getFilename() {
        return getCacheDir()+"/properties.props";
    }
    
    private void load() {
        File f = new File(getFilename());
        if ( !f.exists() ) {
            System.out.println("No properties file found at ["+getFilename()+"] so using defaults");
            return;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(getFilename());
            props.load(fis);
        } catch (Exception e) {
            System.out.println("Failed to load properties in ["+getFilename()+"] with "+e);
            e.printStackTrace();
        } finally {
            if ( fis != null ) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void save() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(getFilename());
            props.store(fos, "");
        } catch (Exception e) {
            System.out.println("Failed to save properties in ["+getFilename()+"] with "+e);
            e.printStackTrace();
        } finally {
            if ( fos != null ) {
                try {
                    fos.close();
                } catch (IOException e) {
                    System.out.println("Failed to close fos for save properties in ["+getFilename()+"] with "+e);
                }
            }
        }
    }

    public static String getCacheDir() {
        String home = System.getenv("HOME");
        String cacheDir = home.endsWith("/") ? home + ".sqzcov" : home + "/.sqzcov";
        File cacheDirF = new File(cacheDir);
        if (!cacheDirF.exists()) {
            cacheDirF.mkdir();
        }
        if (!cacheDirF.isDirectory()) {
            System.out.println("Failed, cannot create directory [" + cacheDir + "]");
            System.exit(1);
            return null;
        }
        return cacheDir;
    }

}
