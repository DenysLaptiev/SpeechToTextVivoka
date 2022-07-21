package com.vivoka.freespeech.assets;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;

import com.vivoka.vsdk.Exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AsrAssetsExtractor extends AsyncTask<Void, Void, Void> {

    private final String _assetsPath;
    private final AssetManager _assetManager;
    private final IAssetsExtractorCallback _callback;

    public AsrAssetsExtractor(Context context, String assetsPath, IAssetsExtractorCallback callback) throws Exception {
        if (context == null) {
            throw new Exception("context is null");
        }

        if (assetsPath == null || assetsPath.isEmpty()) {
            throw new Exception("assetsPath is null or empty");
        }

        if (callback == null) {
            throw new Exception("callback is null");
        }

        this._assetManager = context.getAssets();
        this._assetsPath = assetsPath;
        this._callback = callback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            copyFileOrDir("");
        } catch (IOException e) {
            new Exception(e.getMessage()).printFormattedMessage();
        }
        return null;
    }

    private void copyFileOrDir(String path) throws IOException {
        String[] assets = _assetManager.list(path);
        if (assets.length == 0) {
            copyFile(path);
        } else {
            String fullPath = _assetsPath + path;

            // Don't copy tts (vocalizer) data
            if (fullPath.contains("data/vocalizer")) {
                return;
            }

            File dir = new File(fullPath);
            if (!dir.exists() && !path.startsWith("images") && !path.startsWith("webkit")) {
                if (!dir.mkdirs()) {
                    new Exception("could not create dir: " + fullPath).printFormattedMessage();
                }
            }

            for (String asset : assets) {
                String p;
                if (path.equals("")) {
                    p = "";
                } else {
                    p = path + "/";
                }
                if (!path.startsWith("images") && !path.startsWith("webkit")) {
                    copyFileOrDir(p + asset);
                }
            }
        }
    }

    private void copyFile(String filename) throws IOException {
        InputStream in;
        OutputStream out;
        String newFileName;

        in = _assetManager.open(filename);

        if (filename.contains("cache")) {
            if (!filename.contains("liquid_config.json")) {
                return;
            }
        }

        if (filename.contains("config/")) {
            if (!filename.contains("logger.json")) {
                return;
            }
        }

        if (filename.contains("data")) {
            if (filename.contains("vocalizer")) {
                return;
            }
        }

        newFileName = _assetsPath + filename;
        out = new FileOutputStream(newFileName);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

        in.close();
        out.flush();
        out.close();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {
            _callback.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
