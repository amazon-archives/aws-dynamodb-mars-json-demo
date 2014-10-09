package com.amazonaws.services.dynamodbv2.json.demo.mars.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class WorkerTestUtils {

    public static final String CLASSPATH = System.getProperty("java.class.path");
    public static final String FILE_ENCODING = "UTF-8";

    public static String getPath(final String fileName) {
        final URL url = ClassLoader.getSystemResource(fileName);
        if (url != null) {
            return url.getFile();
        } else {
            throw new IllegalArgumentException("File " + fileName + " does not exist on the classpath: " + CLASSPATH);
        }
    }

    static String readFile(final String file) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(file)));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

}
