package com.jimmy.friday.agent.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtil {

    public static File touch(String fileName) {
        File file = new File(fileName);

        if (null == file) {
            return null;
        }

        if (false == file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * delete directories and files recursively
     *
     * @param dir directory to delete
     */
    public static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!Files.isSymbolicLink(file.toPath())) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    public static void deleteDirectoryOnExit(File dir) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                FileUtil.deleteDirectory(dir);
            }
        });
    }
}
