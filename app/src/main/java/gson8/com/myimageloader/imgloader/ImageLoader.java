package gson8.com.myimageloader.imgloader;

/*
 * MyImageLoader making by Syusuke/琴声悠扬 on 2016/5/24
 * E-Mail: Zyj7810@126.com
 * Package: gson8.com.myimageloader.ImgLoader1.ImageLoader
 * Description: null
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gson8.com.myimageloader.core.DiskLruCache;
import gson8.com.myimageloader.utils.Utils;

public class ImageLoader {

    private static final String TAG = ImageLoader.class.getCanonicalName();
    private LruCache<String, Bitmap> mLruCache;
    private DiskLruCache mDiskLruCache;

    private final Object mDiskCacheLock = new Object();
    private ExecutorService mThreadPool;
    private Thread mThread;

    private static ImageLoader INSTANCE;

    private Handler mIvHandler;


    private ImageLoader(Context context) {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;

        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        try {
            mDiskLruCache =
                    DiskLruCache.open(getDiskCacheDir(context, "ZYJ123"), 1, 1, 10 * 1024 * 1024);
            Log.e("init", "ImageLoader: init");
        } catch(IOException e) {
            e.printStackTrace();
        }

        mThreadPool = Executors.newFixedThreadPool(4);
    }

    public File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    public static ImageLoader getInstance(Context context) {
        if(INSTANCE == null) {
            synchronized(ImageLoader.class) {
                if(INSTANCE == null) {
                    INSTANCE = new ImageLoader(context);
                }
            }
        }
        return INSTANCE;
    }


    public void destoryThreadPool() {
        INSTANCE = null;
        mThreadPool.shutdownNow();
    }

    public void displayImage(final ImageView imageView, final String urlLink,
                             final String referer) {

        imageView.setTag(urlLink);

        Log.e(TAG, "displayImage: ");

        if(mIvHandler == null) {
            mIvHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    Bitmap bitmap = holder.bitmap;
                    String path = holder.urlLink;
                    ImageView iv = holder.imageView;
                    if(iv.getTag().toString().equals(path)) {
                        iv.setImageBitmap(bitmap);
                    }
                }
            };
        }


        final Bitmap bitmap = getBitmapFromLruCache(urlLink);
        if(bitmap != null) {

            Log.e(TAG, "getBitmapFromLruCache not null -------------- ");

            Message msg = Message.obtain();
            ImgBeanHolder holder = new ImgBeanHolder();
            holder.bitmap = bitmap;
            holder.urlLink = urlLink;
            holder.imageView = imageView;
            msg.obj = holder;
            mIvHandler.sendMessage(msg);
        } else {
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = getBitmapFromDirkCache(urlLink);
                    if(bitmap != null) {
                        Log.e(TAG, "run: addBitmapToLruCache() ' not null++++++++++++");
                        addBitmapToLruCache(urlLink, bitmap);

                        Message msg = Message.obtain();
                        ImgBeanHolder holder = new ImgBeanHolder();
                        holder.bitmap = bitmap;
                        holder.urlLink = urlLink;
                        holder.imageView = imageView;
                        msg.obj = holder;
                        mIvHandler.sendMessage(msg);
                    } else {
                        Log.e(TAG, "run: getBitmapFromUrl() start AAAAAAAAAAA");
                        bitmap = getBitmapFromUrl(urlLink, referer);

                        Message msg = Message.obtain();
                        ImgBeanHolder holder = new ImgBeanHolder();
                        holder.bitmap = bitmap;
                        holder.urlLink = urlLink;
                        holder.imageView = imageView;
                        msg.obj = holder;
                        mIvHandler.sendMessage(msg);
                    }
                    Log.e(TAG, "run: it 's Ok");
                }
            });

        }
    }


    public Bitmap getBitmapFromUrl(String urlLink, String referer) {
        Bitmap bitmap = null;
        try {

            URL url = new URL(urlLink);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            bitmap = Utils.decodeSampledBitmapFromStream(bis);

            addBitmapToLruCache(urlLink, bitmap);
            addBitmapToDirkCache(urlLink, bitmap);

            bis.close();
            conn.disconnect();
            return bitmap;
        } catch(IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            Log.e(TAG, "getBitmapFromUrl: 网络有问题");
        }
        return null;
    }


    public void addBitmapToLruCache(String key, Bitmap bitmap) {
        if(getBitmapFromLruCache(key) == null) {
            Log.e(TAG, "addBitmapToLruCache: ");
            mLruCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromLruCache(String key) {
        Log.e(TAG, "getBitmapFromLruCache: " + System.currentTimeMillis());
        return mLruCache.get(key);
    }


    public void addBitmapToDirkCache(String key, Bitmap bitmap) {
        if(getBitmapFromDirkCache(key) == null) {
            Log.e(TAG, "addBitmapToDirkCache: ");
            try {
                DiskLruCache.Editor editor = mDiskLruCache.edit(Utils.MD5(key));

                OutputStream out = editor.newOutputStream(0);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                editor.commit();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

    }


    public Bitmap getBitmapFromDirkCache(String key) {

        Log.e(TAG, "getBitmapFromDirkCache: ");
        synchronized(mDiskCacheLock) {
            try {
                DiskLruCache.Snapshot snapshot = mDiskLruCache.get(Utils.MD5(key));
                if(snapshot != null) {
                    return BitmapFactory.decodeStream(snapshot.getInputStream(0));
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    class ImgBeanHolder {
        String urlLink;
        Bitmap bitmap;
        ImageView imageView;
    }


}
