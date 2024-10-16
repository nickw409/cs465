package utils;

import java.util.*;
import java.io.*;

public class PropertyHandler extends Properties {
    File propertyFile = null;

    public PropertyHandler(String propertyFileString) throws FileNotFoundException, IOException {

        propertyFile = getPropertyFile(propertyFileString);

        InputStream is = new BufferedInputStream(new FileInputStream(propertyFile));
        this.load(is);
        is.close();
    }

    @Override
    public String getProperty(String key) {
        String value = super.getProperty(key);

        return value;
    }

    private File getPropertyFile(String propertyFileString) throws FileNotFoundException, IOException {

        //in current dir
        if ((propertyFile = new File(propertyFileString)).exists()) {
            return propertyFile;
        }

        String dirString = System.getProperty("user.dir");
        String completeString = dirString + File.separator + propertyFileString;
        if((propertyFile = new File(completeString)).exists()) {
            return propertyFile;
        }

        dirString = System.getProperty("user.home");
        completeString = dirString + File.separator + propertyFileString;
        if ((propertyFile = new File(completeString)).exists()) {
            return propertyFile;
        }

        dirString = System.getProperty("java.home") + File.separator + "lib";
        completeString = dirString + File.separator + propertyFileString;
        if ((propertyFile = new File(completeString)).exists()) {
            return propertyFile;
        }

        throw new FileNotFoundException("[PropertyHandler.Propertyhandler] Configuration File \"" + propertyFile );
    }
}
