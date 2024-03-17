package com.cyclopes;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.FileUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * Hello world!
 *
 */
public class UnusedProperties
{
    private static final Logger logger = LogManager.getLogger("UUS");
    private static final String[] DIVIDER = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    private static final String PROP_FILE_NAME = "application.properties";

    private static final Map<String, String> KEY_MAP = new HashMap<>();
    private static List<String> SEARCH_TARGET_EXTENSION;

    private static final Map<String, String> USED_KEY_MAP = new HashMap<>();
    private static String OUTPUT_USED_PATH = "";
    private static String OUTPUT_UNUSED_PATH = "";

    public static void main(String[] args) throws Exception
    {
        Properties properties = new Properties();
        try {
            properties.load(java.nio.file.Files.newInputStream(Paths.get(PROP_FILE_NAME)));
        } catch (IOException e) {
            properties.load(UnusedProperties.class.getClassLoader().getResourceAsStream(PROP_FILE_NAME));
        }

        String keyPath = properties.getProperty("key.path", "");
        String searchTargetPath = properties.getProperty("search.target.path", "");
        OUTPUT_USED_PATH = properties.getProperty("output.used.path", "");
        OUTPUT_UNUSED_PATH = properties.getProperty("output.unused.path", "");
        SEARCH_TARGET_EXTENSION = Arrays.asList(properties.getProperty("search.target.extension", "").split(","));


        File keyFile = new File(keyPath);
        boolean checkKeyFile = StringUtils.isNotBlank(keyPath) && keyFile.exists() && keyFile.isFile();

        File targetFolder = new File(searchTargetPath);
        boolean checkTargetFolder = StringUtils.isNotBlank(searchTargetPath) && targetFolder.exists() && targetFolder.isDirectory();

        logger.info("=======================================");
        logger.info("keyPath: {}", keyPath);
        logger.info("key file check: {}", checkKeyFile);
        logger.info("targetPath: {}", searchTargetPath);
        logger.info("target folder check: {}", checkTargetFolder);
        logger.info("target extension: {}", SEARCH_TARGET_EXTENSION);
        logger.info("=======================================");
        logger.info("");

        if(checkKeyFile) {
            logger.info("generate keymap start ...");
            generateKeyMap(keyFile);
            logger.info("generate keymap end");

            if(checkTargetFolder && !KEY_MAP.isEmpty()) {
                logger.info("search start ...");
                searchSubFile(targetFolder, 0);
                logger.info("search end");
            }

            if(!USED_KEY_MAP.isEmpty()) {
                logger.info("generate result start ...");
                makeResultFile();
                logger.info("generate result end");
            }
        }
    }

    private static void generateKeyMap(File file) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (StringUtils.isNotBlank(line)) {
                    String[] split = line.split("=");
                    boolean isKey = line.indexOf("=") > 0;

                    if(isKey) {
                        String key = ArrayUtils.get(split, 0, "");
                        String value = ArrayUtils.get(split, 1, "");
                        if (StringUtils.isNotBlank(key)) {
                            KEY_MAP.put(key, value);
                        }
                    }
                }
            }
        }
    }

    private static void checkKeyInFile(File file) {
        String fileContent = getFileContent(file);
        for (Map.Entry<String, String> entry : KEY_MAP.entrySet()) {
            String key1 = "\"" + entry.getKey() + "\"";
            String key2 = "'" + entry.getKey() + "'";
            if(fileContent.contains(key1) || fileContent.contains(key2)) {
                logger.info("find key: {}", entry.getKey());
                logger.info("file path: {}", file.getPath());

                USED_KEY_MAP.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private static void searchSubFile(File folder, int folderLevel) {
        File[] subFiles = folder.listFiles();
        int subFileCount = (subFiles != null) ? subFiles.length : 0;

        logger.debug("subfile count: {}", subFileCount);
        if(subFileCount>0) {
            logger.debug(StringUtils.leftPad("", 30, DIVIDER[folderLevel]));
            for(File file: subFiles) {
                String fileName = file.getName();
                boolean isDirectory = file.isDirectory();
                boolean isFile = file.isFile();
                logger.debug("file name: {}", fileName);
                logger.debug("file is folder: {}", isDirectory);
                if(isDirectory) {
                    searchSubFile(file, folderLevel+1);
                } else if(isFile) {
                    String extension = FileUtils.getFileExtension(file);
                    logger.debug("file extension: {}", extension);

                    if(SEARCH_TARGET_EXTENSION.contains(extension)) {
                        checkKeyInFile(file);
                    }
                }
                logger.debug(StringUtils.leftPad("", 30, DIVIDER[folderLevel]));
            }
        }
    }

    private static void makeResultFile() throws IOException {
        File usedFile = new File(OUTPUT_USED_PATH);
        File unusedFile = new File(OUTPUT_UNUSED_PATH);

        boolean usedFileCreated;
        if(!usedFile.exists()) {
            usedFileCreated = usedFile.createNewFile();
        } else {
            usedFileCreated = true;
        }

        boolean unusedFileCreated;
        if(!unusedFile.exists()) {
            unusedFileCreated = unusedFile.createNewFile();
        } else {
            unusedFileCreated = true;
        }

        if(usedFileCreated && unusedFileCreated) {
            try (FileWriter usedFileWriter = new FileWriter(usedFile);
                 FileWriter unusedFileWriter = new FileWriter(unusedFile)) {
                for (Map.Entry<String, String> entry : KEY_MAP.entrySet()) {
                    String text = entry.getKey() + "=" + entry.getValue() + System.lineSeparator();
                    String value = USED_KEY_MAP.get(entry.getKey());
                    if(value == null) {
                        unusedFileWriter.write(text);
                    } else {
                        usedFileWriter.write(text);
                    }
                }
            }
        }
    }

    private static String getFileContent(File file) {
        try {
            return Files.asCharSource(file, Charsets.UTF_8).read();
        } catch (IOException e) {
            //nothing
        }

        return "";
    }
}
