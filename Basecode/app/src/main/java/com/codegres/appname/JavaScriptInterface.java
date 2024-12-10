package com.codegres.appname;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JavaScriptInterface {
    private Context context;

    public JavaScriptInterface(Context context) {
        this.context = context;
    }

    /**
     * Method to process Base64 data then save it locally.
     * <p>
     * 1. Strip Base64 prefix from Base64 data
     * 2. Decode Base64 data
     * 3. Write Base64 data to file based on mime type located in prefix
     * 4. Save file locally
     */
    @JavascriptInterface
    public void processBase64Data(String base64Data) {
        Log.i("JavaScriptInterface", "Processing base64Data ...");

        Log.i("JavaScriptInterface", "base64Data: " + base64Data);

        String fileName = "";
        String bytes = "";

        if (base64Data.startsWith("data:video/webm;base64,")) {
            String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            fileName = "video_editor_" + dateTime + ".webm";
            bytes = base64Data.replaceFirst("data:video/webm;base64,", "");
        }else{
            System.out.println("Invalid data 1");
        }

        if (!fileName.isEmpty() && !bytes.isEmpty()) {

            byte[] decodedString = Base64.decode(bytes, Base64.DEFAULT);
            configureVideoEncoder();
            saveToMediaStore(fileName, decodedString);
            /*File downloadPath = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
            );

            Log.i("JavaScriptInterface", "Download Path: " + downloadPath.getAbsolutePath());

            try {
                byte[] decodedString = Base64.decode(bytes, Base64.DEFAULT);
                FileOutputStream os = new FileOutputStream(downloadPath, false);
                os.write(decodedString);
                os.flush();
                os.close();

                // Notify user and system that the file was saved
                Toast.makeText(context, "File saved" , Toast.LENGTH_LONG).show();
                MediaScannerConnection.scanFile(context,
                        new String[]{downloadPath.getAbsolutePath()},
                        null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("JavaScriptInterface", "Scanned " + path + ":");
                                Log.i("JavaScriptInterface", "-> uri=" + uri);
                            }
                        });

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Failed to save file", Toast.LENGTH_LONG).show();
            }*/
        }else{
            System.out.println("Invalid data 2");
        }
    }

    private void saveToMediaStore(String fileName, byte[] data) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/webm");
        values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);

        Uri uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            outputStream.write(data);
            Toast.makeText(context, "File saved to MediaStore", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save file", Toast.LENGTH_LONG).show();
        }
    }

    private void configureVideoEncoder() {
        try {
            MediaFormat format = MediaFormat.createVideoFormat("video/mp4", 1280, 720); // Use a lower resolution
            format.setInteger(MediaFormat.KEY_BIT_RATE, 1000000); // Set a lower bitrate
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30); // Keep frame rate at 30
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2); // Set a reasonable I-frame interval

            // Create and configure the encoder
            MediaCodec encoder = MediaCodec.createEncoderByType("video/mp4");
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();
        }catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            Toast.makeText(context, "Failed to configure video encoder", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method to convert blobUrl to Blob, then process Base64 data on native side
     * <p>
     * 1. Download Blob URL as Blob object
     * 2. Convert Blob object to Base64 data
     * 3. Pass Base64 data to Android layer for processing
     */
    public String getBase64StringFromBlobUrl(String blobUrl) {
        Log.i("JavaScriptInterface", "Downloading " + blobUrl + " ...");

        // Script to convert blob URL to Base64 data in Web layer, then process it in Android layer
        String script = "javascript: (() => {" +
                "async function getBase64StringFromBlobUrl() {" +
                "const xhr = new XMLHttpRequest();" +
                "xhr.open('GET', '" + blobUrl + "', true);" +
                "xhr.responseType = 'blob';" + // Ensure we are expecting a blob response
                "xhr.onload = () => {" +
                "if (xhr.status === 200) {" +
                "const blobResponse = xhr.response;" +
                "const fileReaderInstance = new FileReader();" +
                "fileReaderInstance.readAsDataURL(blobResponse);" +
                "fileReaderInstance.onloadend = () => {" +
                "console.log('Downloaded " + blobUrl + " successfully!');" +
                "const base64data = fileReaderInstance.result;" +
                "console.log(base64data);" +
                "Android.processBase64Data(base64data);" + // Call the Android interface
                "};" + // file reader on load end
                "}" + // if
                "};" + // xhr on load
                "xhr.onerror = () => {" + // Handle errors
                "console.error('Error downloading video: ' + xhr.statusText);" +
                "};" +
                "xhr.send();" +
                "}" + // async function
                "getBase64StringFromBlobUrl();" +
                "})()";

        return script;
    }
}
