package com.example.aliyunlivedemo.live.push.push_3_0;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;

import com.example.aliyunlivedemo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Common {

    private static String SD_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator;
    private static String filename = "watermark.png";
    public static final String waterMark = SD_DIR + filename;

    public static void copyAsset(Context context) {
        if(new File(SD_DIR + filename).exists()) {
            return;
        }
        if(SD_DIR != null && new File(SD_DIR).exists()) {

            Boolean isSuccess = true;
            AssetManager assetManager = context.getAssets();

            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                String newFileName = SD_DIR + filename;
                out = new FileOutputStream(newFileName);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
                e.printStackTrace();
                isSuccess = false;
            }
        }
    }

    public static void showDialog(final Context context, final String message) {
        if(context == null || message == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(context != null) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle(context.getString(R.string.dialog_title));
                    dialog.setMessage(message);
                    dialog.setNegativeButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

}
