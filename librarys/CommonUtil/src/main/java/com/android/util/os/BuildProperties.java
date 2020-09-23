package com.android.util.os;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class BuildProperties {
    private final Properties properties = new Properties();

    private BuildProperties() throws IOException {
        this.properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
    }

    public boolean containsKey(Object key) {
        return this.properties.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.properties.containsValue(value);
    }

    public Set<Map.Entry<Object, Object>> entrySet() {
        return this.properties.entrySet();
    }

    public String getProperty(String name) {
        return this.properties.getProperty(name);
    }

    public String getProperty(String name, String defaultValue) {
        return this.properties.getProperty(name, defaultValue);
    }

    public boolean isEmpty() {
        return this.properties.isEmpty();
    }

    public Enumeration<Object> keys() {
        return this.properties.keys();
    }

    public Set<Object> keySet() {
        return this.properties.keySet();
    }

    public int size() {
        return this.properties.size();
    }

    public Collection<Object> values() {
        return this.properties.values();
    }

    public static BuildProperties newInstance() throws IOException {
        return new BuildProperties();
    }
}