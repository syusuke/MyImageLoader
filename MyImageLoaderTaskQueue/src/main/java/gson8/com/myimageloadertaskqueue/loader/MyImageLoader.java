package gson8.com.myimageloadertaskqueue.loader;

/*
 * MyImageLoader making by Syusuke/琴声悠扬 on 2016/5/28
 * E-Mail: Zyj7810@126.com
 * Package: gson8.com.myimageloader.imgloaderadvance.MyImageLoader
 * Description: null
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import gson8.com.myimageloadertaskqueue.config.LoaderConfig;

import gson8.com.myimageloadertaskqueue.core.DiskLruCache;
import gson8.com.myimageloadertaskqueue.util.ImageLoaderUtils;


public class MyImageLoader {

    private static final String TAG = MyImageLoader.class.getName();

    private static MyImageLoader mInstance;

    private LruCache<String, Bitmap> mLruCache;
    private DiskLruCache mDiskLruCache;

    private Semaphore mSemaphoreThreadPool;
    private ExecutorService mThreadPool;
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;

    private LinkedList<Runnable> mTaskQueue;
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);

    private LoaderConfig mConfig;


    private Handler mUIHandler;


    /**
     * 单例模式
     *
     * @return 单例对象
     */
    public static MyImageLoader getInstance(LoaderConfig config) {

        if(mInstance == null) {
            synchronized(MyImageLoader.class) {
                if(mInstance == null)
                    mInstance = new MyImageLoader(config);
            }
        }
        return mInstance;
    }


    private MyImageLoader(LoaderConfig config) {
        this.mConfig = config;
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        mThreadPool.execute(getTask());

                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        mLruCache = new LruCache<String, Bitmap>(mConfig.getLruCacheSize()) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        try {
            mDiskLruCache = DiskLruCache
                    .open(mConfig.getDiskCacheFolder(), mConfig.getAppVerstion(), 1,
                            mConfig.getDiskLruCacheSize());
        } catch(IOException e) {
            e.printStackTrace();
        }

        mThreadPool = Executors.newFixedThreadPool(mConfig.getThreadCount());
        mTaskQueue = new LinkedList<>();

        mSemaphoreThreadPool = new Semaphore(mConfig.getThreadCount());
    }

    public void displayImage(final String link, final ImageView imageView) {
        imageView.setTag(link);
        initUIHandler();

        Bitmap bm = getBitmapFromLruCache(link);

        if(bm != null)
            refreshBitmap(link, imageView, bm);
        else
            addTask(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = getBitmapFromDisLruCache(link);
                            if(bitmap == null) {
                                bitmap = getBitmapFromURL(link);
                            }
                            addBitmapToLruCache(link, bitmap);
                            addBitmapToDirkLruCache(link, bitmap);

                            refreshBitmap(link, imageView, bitmap);


                            //TODO 原来是这里的啊
                            mSemaphoreThreadPool.release();
                        }
                    }
            );

    }


    private Runnable getTask() {
        if(mConfig.getTaskType() == LoaderConfig.TaskType.TYPE_LIFO) {
            return mTaskQueue.removeLast();
        } else if(mConfig.getTaskType() == LoaderConfig.TaskType.TYPE_FIFO) {
            return mTaskQueue.removeFirst();
        }
        return null;
    }

    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);

        try {
            if(mPoolThreadHandler == null) {
                mSemaphorePoolThreadHandler.acquire();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(110);
    }

    private void refreshBitmap(String link, ImageView iv, Bitmap bm) {
        Message msg = Message.obtain();
        ImgBeanHolder holder = new ImgBeanHolder();
        holder.bitmap = bm;
        holder.imageView = iv;
        holder.link = link;
        msg.obj = holder;
        mUIHandler.sendMessage(msg);
    }


    private void initUIHandler() {
        if(mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;

                    Bitmap bitmap = holder.bitmap;
                    ImageView iv = holder.imageView;
                    String link = holder.link;

                    if(iv.getTag().toString().equals(link)) {
                        iv.setImageBitmap(bitmap);
                    }

                }
            };
        }
    }


    /**
     * 从网络中获取图片
     *
     * @param link
     * @return
     */
    private Bitmap getBitmapFromURL(String link) {

        Bitmap bitmap = null;
        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            bitmap = ImageLoaderUtils.decodeSampledBitmapFromStream(bis);
            bis.close();
            conn.disconnect();
            return bitmap;
        } catch(IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            Log.e(TAG, "getBitmapFromUrl: 网络有问题吧");
        }
        return null;
    }


    /**
     * 添加到缓存
     *
     * @param link
     * @param bitmap
     */
    private void addBitmapToLruCache(String link, Bitmap bitmap) {
        if(getBitmapFromLruCache(link) == null) {
            if(bitmap != null)
                mLruCache.put(link, bitmap);
        }
    }

    /**
     * 从缓存中获取图片
     *
     * @param link
     * @return
     */
    private Bitmap getBitmapFromLruCache(String link) {
        return mLruCache.get(link);
    }

    /**
     * 从磁盘缓存中获取
     *
     * @param link
     * @return
     */
    private Bitmap getBitmapFromDisLruCache(String link) {
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(ImageLoaderUtils.MD5(link));
            if(snapshot != null) {
                return BitmapFactory.decodeStream(snapshot.getInputStream(0));
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加到磁盘缓存
     *
     * @param link
     * @param bitmap
     */
    private void addBitmapToDirkLruCache(String link, Bitmap bitmap) {
        if(bitmap != null) {
            if(getBitmapFromDisLruCache(link) == null) {
                try {
                    DiskLruCache.Editor editor = mDiskLruCache.edit(ImageLoaderUtils.MD5(link));
                    OutputStream os = editor.newOutputStream(0);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();
                    editor.commit();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String link;
    }

   /* private class ImageSize {
        int width;
        int height;
    }*/


}
