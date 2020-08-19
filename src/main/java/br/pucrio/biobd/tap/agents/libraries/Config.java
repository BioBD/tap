/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.libraries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Rafael
 *
 */
public class Config {

    private static final Properties PARAMETERS = new Properties();

    public static String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    public static boolean containsKey(String key) {
        return getProperties().containsKey(key);
    }

    public static Properties getProperties() {
        Properties prop;
        if (PARAMETERS.isEmpty()) {
            try {
                System.out.println("Reading Property File from: config/parameters.properties");
                prop = readPropertyFile("config/parameters.properties");
                if (prop != null) {
                    PARAMETERS.putAll(prop);
                }
                System.out.println("Reading Property File from: config/sql.properties");
                prop = readPropertyFile("config/sql.properties");
                if (prop != null) {
                    PARAMETERS.putAll(prop);
                }
            } catch (IOException e) {
                System.err.print(e);
            }
        }
        return PARAMETERS;
    }

    public static Properties readPropertyFile(String filename) throws IOException {
        Properties properties = new Properties();
        File propertyFile = new File(filename);
        if (!propertyFile.exists()) {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            if (is != null) {
                properties.load(is);
            } else {
                System.err.println("File " + filename + " does not exist");
                throw new FileNotFoundException();
            }
        } else {
            FileInputStream fi = new FileInputStream(propertyFile);
            properties.load(fi);
            fi.close();
        }

        return properties;
    }

}
