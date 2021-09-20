package com.peter.mypusheditvideo.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class FileUtil {

    public static File createFile(String name) {
        String dirPath = Environment.getExternalStorageDirectory().getPath()+"/AudioRecord/";
        File file = new File(dirPath);
        if(!file.exists()) {
            file.mkdirs();
        }
        String filePath = dirPath +name;
        File objFile = new File(filePath);
        if (!objFile.exists()) {
            try {
                objFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return objFile;
        }
        return null;
    }

}
