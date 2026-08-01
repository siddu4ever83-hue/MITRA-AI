package com.mitra.ai;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FileManager {

    private Context context;
    private Uri currentUri;

    public FileManager(Context context) {
        this.context = context;
    }

    public String readFile(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            inputStream.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveFileToUri(Uri uri, String content) {
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri, "wt");
            if (outputStream == null) return false;
            outputStream.write(content.getBytes());
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveFile(String content) {
        if (currentUri != null) {
            return saveFileToUri(currentUri, content);
        }
        return false;
    }

    public void setCurrentUri(Uri uri) {
        this.currentUri = uri;
    }

    public Uri getCurrentUri() {
        return currentUri;
    }

    public String getCurrentFileName() {
        if (currentUri != null) {
            String path = currentUri.getLastPathSegment();
            if (path != null && path.contains("/")) {
                return path.substring(path.lastIndexOf("/") + 1);
            }
            return path;
        }
        return null;
    }
}
