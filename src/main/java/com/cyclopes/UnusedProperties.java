package com.cyclopes;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hello world!
 *
 */
public class UnusedProperties
{
    private static final Logger logger = LogManager.getLogger();
    private static final String[] DIVIDER = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};

    private static final String KEY_PATH = "C:/Users/cyclo/Desktop/key.properties";
    private static final List<String> KEY_LIST = new ArrayList<>();

    private static final String SEARCH_TARGET_PATH = "C:/Users/cyclo/Desktop/test";
    private static final List<String> SEARCH_TARGET_EXTENSION = Arrays.asList("jsp", "java");

    public static void main( String[] args )
    {
        File keyFile = new File(KEY_PATH);
        boolean checkKeyFile = keyFile.exists() && keyFile.isFile();

        File targetFolder = new File(SEARCH_TARGET_PATH);
        boolean checkTargetFolder = targetFolder.exists() && targetFolder.isDirectory();

        logger.info("=======================================");
        logger.info("keyPath: {}", SEARCH_TARGET_PATH);
        logger.info("key file check: {}", checkKeyFile);
        logger.info("targetPath: {}", SEARCH_TARGET_PATH);
        logger.info("target folder check: {}", checkTargetFolder);
        logger.info("=======================================");
        logger.info("");

        if(checkKeyFile) {
            makeKeyList(keyFile);

            if(checkTargetFolder && !KEY_LIST.isEmpty()) {
                searchSubFile(targetFolder, 0);
            }
        }
    }

    private static void makeKeyList(File file) {
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            for(String line; (line = br.readLine()) != null; ) {
                if(StringUtils.isNotBlank(line)) {
                    String[] split = line.split("=");
                    boolean isKey = line.indexOf("=") > 0;
                    if(isKey) {
                        KEY_LIST.add(split[0]);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkKeyInFile(File file) {
        String fileContent = getFileContent(file);
        for(String key:KEY_LIST) {
            if(fileContent.contains(key)) {
                logger.info("find key: {}", key);
                logger.info("file path: {}", file.getPath());
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

    private static String getFileContent(File file) {
        try {
            return Files.asCharSource(file, Charsets.UTF_8).read();
        } catch (IOException e) {
            //nothing
        }

        return "";
    }
}
