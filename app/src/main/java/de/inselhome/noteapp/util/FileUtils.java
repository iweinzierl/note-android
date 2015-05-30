package de.inselhome.noteapp.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.inselhome.android.logging.AndroidLoggerFactory;

public final class FileUtils {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("FileUtils");

    public static boolean toFile(final File destination, final String content) {
        try {
            if (destination.exists()) {
                destination.delete();
            }

            IOUtils.write(content, new FileOutputStream(destination));
            return true;
        } catch (IOException e) {
            LOGGER.error("Unable to write content to file", e);
        }

        return false;
    }

    public static String fromFile(final File source) {
        try {
            return org.apache.commons.io.FileUtils.readFileToString(source);
        } catch (IOException e) {
            LOGGER.error("Unable to read content from file", e);
        }

        return null;
    }
}
