/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.helper;

import com.google.common.base.Strings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author i330120
 */
public class EnvHelper {

    protected Properties config;
    protected final String name;

    public EnvHelper(String name) {
        this.name = name;
    }

    public static String getSystemEnv(String name) {
        return System.getenv(name);
    }

    public static String getSystemProperty(String name) {
        return System.getProperty(name);
    }

    public static String getSystemAny(String name) {
        String env = System.getProperty(name);
        return (env == null)
                ? System.getenv(name)
                : env;
    }

    protected Properties parsePropertyFile(String path) throws FileNotFoundException, IOException {
        FileReader reader = null;
        File f = new File(path);

        reader = new FileReader(f);
        Properties p = new Properties(getDefaultProperties());
        try {
            p.load(reader);
        } finally {
            reader.close();
        }
        return p;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public Properties getDefaultProperties() {
        Properties p = new Properties();
        
        p.put("service.limit.user.size", "10000000000"); // 10 GB

        p.put("service.validation.user.email.regex", "^[^@]+[@][^@]+[.][^@]+$");

        p.put("service.validation.collection.name.regex", "^(\\w|-|\\s)+$");
        p.put("service.validation.collection.name.length", "256");

        p.put("service.validation.user.name.regex", "^(\\w|-|\\s)+$");
        p.put("service.validation.user.name.length", "256");
        
        p.put("service.validation.comment.text.length", "5000");

        p.put("service.environment", "prod");

        p.put("service.cookie.enabled", "false");
        p.put("service.cookie.domain", "localhost");
        p.put("service.cookie.maxage", "900");
        p.put("service.cookie.http", "true");
        p.put("service.cookie.secure", "false");

        p.put("service.cookie.auth.name", "auth");
        p.put("service.cookie.refresh.name", "refresh");

        p.put("service.jwt.auth.exp", "60"); // 1 Hour
        p.put("service.jwt.refresh.exp", "43200");    // 30 Days
        p.put("service.jwt.iss", "picdrop");
        p.put("service.jwt.aud", "picdrop/app");

        p.put("token.signer.alg", "HS256");
        p.put("token.cipher.alg", "dir");
        p.put("token.cipher.meth", "A128CBC-HS256");

        p.put("service.file.store", "");

        p.put("service.upload.store", "");
        p.put("service.upload.maxmemory", "2000000"); // 2 MB
        p.put("service.upload.maxfilesize", "10000000"); // 10 MB
        p.put("service.upload.maxrequestsize", "100000000"); // 100 MB

        p.put("service.json.view", "public");

        p.put("service.tika.config", "");

        return p;
    }

    public Properties getProperties() throws IOException {
        if (config == null) {
            String env = getSystemAny(this.name);
            if (Strings.isNullOrEmpty(env)) {
                throw new IOException(String.format("No env set for '%s'", this.name));
            }
            config = parsePropertyFile(env);
        }
        return config;
    }

    public Properties getPropertiesWithDefault() {
        try {
            return getProperties();
        } catch (IOException ex) {
            return getDefaultProperties();
        }
    }

    public static EnvHelper from(String env) {
        return new EnvHelper(env);
    }
}
