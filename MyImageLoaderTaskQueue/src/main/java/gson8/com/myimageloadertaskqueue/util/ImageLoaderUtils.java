package gson8.com.myimageloadertaskqueue.util;

/*
 * MyImageLoader making by Syusuke/琴声悠扬 on 2016/5/28
 * E-Mail: Zyj7810@126.com
 * Package: gson8.com.myimageloadertaskqueue.util.ImageLoaderUtils
 * Description: null
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;

public class ImageLoaderUtils {

    public static Bitmap decodeSampledBitmapFromStream(InputStream is) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 2;
        return BitmapFactory.decodeStream(is, null, options);
    }

    /**
     * 获取 DiskLruCache 的缓存路径
     * /sccrad/Andorid/data/<package name>/cache/DiskLruCacheFolder
     *
     * @param context
     * @return
     */
    public static File getDiskCacheDir(Context context, String name) {

        if(name == null || name.equals("")) {
            name = "DiskLruCacheFolder";
        }


        String cachePath;
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + name);
    }

    public static String MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for(int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];

        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();

        for(int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if(val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }

        return hexValue.toString();
    }
}
