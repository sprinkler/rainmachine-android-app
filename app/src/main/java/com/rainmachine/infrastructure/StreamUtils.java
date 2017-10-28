package com.rainmachine.infrastructure;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import timber.log.Timber;

/**
 * Common utilities to process streams
 *
 * @author ka
 */
public class StreamUtils {

    /**
     * Saves an InputStream to a file
     */
    public static boolean saveFile(File file, InputStream is) {
        try {
            OutputStream os = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            os.close();
            is.close();
            return true;
        } catch (IOException ioe) {
            Timber.w(ioe, ioe.getMessage());
        }
        return false;
    }

    public static byte[] readFile(String imagePath) {
        try {
            File file = new File(imagePath);
            InputStream is = new FileInputStream(file);
            byte buf[] = new byte[(int) file.length()];
            is.read(buf);
            is.close();
            return buf;
        } catch (IOException ioe) {
            Timber.w(ioe, ioe.getMessage());
        }
        return null;
    }

    /**
     * Convert Bitmap to byte[]
     */
    public static byte[] bytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static String toString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static Bitmap getScaledDownBitmap(String file, int side) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);

        options.inSampleSize = calculateInSampleSizeForLargestSide(options, side);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file, options);
    }

    private static int calculateInSampleSizeForLargestSide(BitmapFactory.Options options, int
            side) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;
        if (width > height) {
            if (width > side) {
                inSampleSize = Math.round((float) width / (float) side);
            }
        } else {
            if (height > side) {
                inSampleSize = Math.round((float) height / (float) side);
            }
        }
        return inSampleSize;
    }
}
