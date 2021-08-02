package com.biubiu.dms.core.jdbc;

import com.biubiu.dms.core.utils.MD5Util;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class ExtendedJdbcClassLoader extends URLClassLoader {
    private static final String JAR_FILE_SUFFIX = ".jar";

    private static volatile Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();
    private static volatile Map<String, ExtendedJdbcClassLoader> classLoaderMap = new HashMap<String, ExtendedJdbcClassLoader>();

    private ExtendedJdbcClassLoader(URL[] urls) {
        super(urls);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> aClass = classMap.get(name);
        if (aClass != null) {
            return aClass;
        }

        Class<?> findClass = null;
        try {
            findClass = findClass(name);
        } catch (Exception e) {
        }
        if (findClass != null) {
            classMap.put(name, findClass);
            return findClass;
        }
        return super.loadClass(name);
    }

    @Override
    protected Package getPackage(String name) {
        return null;
    }


    public static synchronized ExtendedJdbcClassLoader getExtJdbcClassLoader(String path) {
        String key = MD5Util.getMD5(path, false, 32);
        if (classLoaderMap.containsKey(key) && classLoaderMap.get(key) != null) {
            return classLoaderMap.get(key);
        }
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        if (file.isFile() && !file.getName().toLowerCase().endsWith(JAR_FILE_SUFFIX)) {
            return null;
        }
        if (file.isDirectory()) {
            File[] cfiles = file.listFiles();
            if (cfiles == null || cfiles.length == 0) {
                return null;
            }
            URL[] urls = new URL[cfiles.length];
            for (int i = 0; i < cfiles.length; i++) {
                File cfile = cfiles[i];
                if (!cfile.getName().toLowerCase().endsWith(JAR_FILE_SUFFIX)) {
                    continue;
                }
                try {
                    urls[i] = cfile.toURI().toURL();
                } catch (MalformedURLException e) {
                }
            }
            ExtendedJdbcClassLoader extendedJdbcClassLoader = new ExtendedJdbcClassLoader(urls);
            classLoaderMap.put(key, extendedJdbcClassLoader);
            return extendedJdbcClassLoader;
        }
        return null;
    }
}
