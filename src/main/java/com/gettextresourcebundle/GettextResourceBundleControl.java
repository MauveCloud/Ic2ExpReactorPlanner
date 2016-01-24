/**
 * Copyright (c) 2012 Tom Schaible
 * See the file license.txt for copying permission.
 */
package com.gettextresourcebundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Tom Schaible
 *
 *         A resource bundle control for loading gettext po files
 *
 *         This expects po files to be stored in a "location" and named with a "domain"
 *
 *         A po file with a "locale" of en_US a "location" of locale
 *         and a "domain" of lang will be retrievied from:
 *
 *         location/en_US/lang.po
 */
public class GettextResourceBundleControl extends Control {

    private static final Map<String,GettextResourceBundleControl> resourceBundles = new ConcurrentHashMap<String,GettextResourceBundleControl>();

    /**
     * recheck files every 60 seconds
     */
    private static long CACHE_TTL = 60*1000l;

    private Map<File,Long> fileLoadTimes;


    private String domain;

    public synchronized static GettextResourceBundleControl getControl(String domain)
    {
        GettextResourceBundleControl control = resourceBundles.get(domain);
        if ( control == null )
        {
            control = new GettextResourceBundleControl(domain);
            resourceBundles.put(domain, control);
        }
        return control;
    }

    public static void setCacheTTL(long cacheTTL){
        CACHE_TTL = cacheTTL;
    }


    /**
     * @param location the location (directory or classpath location) where the po files are stored
     * @param domain the domain name to use for po files
     */
    private GettextResourceBundleControl(String domain) {
        super();
        this.domain = domain;
        fileLoadTimes = new ConcurrentHashMap<File,Long>();
    }

    @Override
    public List<String> getFormats(String baseName) {
        if (baseName == null)
            throw new NullPointerException();
        return Arrays.asList("po");
    }

    /* (non-Javadoc)
     * @see java.util.ResourceBundle.Control#newBundle(java.lang.String, java.util.Locale, java.lang.String, java.lang.ClassLoader, boolean)
     */
    @Override
    public ResourceBundle newBundle(String baseName, Locale locale,
                                    String format, ClassLoader loader, boolean reload) throws IOException {
        if (baseName == null || locale == null
                || format == null || loader == null)
            throw new NullPointerException();
        ResourceBundle resourceBundle = null;
        if ( format.equals("po") )
        {
            String bundleName = toPoBundleName(baseName,locale);
            String resourceName = toPoResourceName(bundleName,format);

            File file = new File(resourceName);
            URL url = loader.getResource(resourceName);
            if ( file.exists() ){
                resourceBundle = new GettextResourceBundle(file);
                fileLoadTimes.put(file, file.lastModified());
            }
            else if ( url != null ){
                InputStream stream = null;
                if ( reload ) {
                    URLConnection connection = url.openConnection();
                    if ( connection != null ) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
                else {
                    stream = loader.getResourceAsStream(resourceName);
                }

                if ( stream != null ) {
                    resourceBundle = new GettextResourceBundle(stream);
                }

            }

        }
        return resourceBundle;
    }


    protected String toPoResourceName(String bundleName, String format){
        if (bundleName == null || format == null )
            throw new NullPointerException();
        return bundleName+"/LC_MESSAGES/"+this.domain+".po";
    }

    protected String toPoBundleName(String baseName, Locale locale){
        if (baseName == null || locale == null )
            throw new NullPointerException();
        return baseName+"/"+locale.toString();
    }

    @Override
    public long getTimeToLive(String baseName, Locale locale) {
        return GettextResourceBundleControl.CACHE_TTL;
    }

    @Override
    public boolean needsReload(String baseName, Locale locale, String format,
                               ClassLoader loader, ResourceBundle bundle, long loadTime) {
        boolean needsReload = false;

        if ( format.equals("po") )
        {
            String bundleName = toBundleName(baseName,locale);
            String resourceName = toPoResourceName(bundleName,format);

            File file = new File(resourceName);
            //only attempt to reload files
            if ( file.exists() ) {
                //reload if no lastModifed time is tracked or it does not match the current
                //last modified time of the file
                if ( fileLoadTimes == null || fileLoadTimes.get(file) != file.lastModified() ) {
                    needsReload = true;
                }

            }
        }

        return needsReload = true;
    }


}