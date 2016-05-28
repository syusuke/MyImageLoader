package gson8.com.imoocimageloader.util;

/*
 * MyImageLoader making by Syusuke/琴声悠扬 on 2016/5/24
 * E-Mail: Zyj7810@126.com
 * Package: gson8.com.imoocimageloader.util.ImageLoader
 * Description: null
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ImageLoader {

    private static ImageLoader mInstance;


    private LruCache<String, Bitmap> mLrucache;

    private ExecutorService mThreadPool;
    private static final int DEFAULT_THREAD_COUNT = 4;

    private Type mType = Type.LIFO;

    private LinkedList<Runnable> mTaskQueue;

    private Thread mPoolThread;
    private Handler mPoolThreadHandler;

    private Handler mUIHandler;


    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);

    private Semaphore mSemaphoreThreadPool;

    public enum Type {
        FIFO, LIFO;
    }

    private ImageLoader(int mThreadCount, Type type) {
        init(mThreadCount, type);
    }

    private void init(int mThreadCount, Type type) {

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

        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;

        mLrucache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        mThreadPool = Executors.newFixedThreadPool(mThreadCount);
        mTaskQueue = new LinkedList<Runnable>();
        mType = type;

        mSemaphoreThreadPool = new Semaphore(mThreadCount);
    }

    private Runnable getTask() {
        if(mType == Type.FIFO) {
            mTaskQueue.removeFirst();
        } else if(mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }

    public static ImageLoader getInstance(int threadCount, Type type) {
        if(mInstance == null) {
            synchronized(ImageLoader.class) {
                if(mInstance == null)
                    mInstance = new ImageLoader(threadCount, type);
            }
        }
        return mInstance;
    }


    /**
     * LoadImage
     *
     * @param path
     * @param imageView
     */
    public void loadImage(final String path, final ImageView imageView) {

        imageView.setTag(path);

        if(mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    Bitmap bitmap = holder.bitmap;
                    ImageView imageView1 = holder.imageView;
                    String path = holder.path;

                    if(imageView1.getTag().toString().equals(path)) {
                        imageView1.setImageBitmap(bitmap);
                    }
                }
            };
        }

        Bitmap bm = getBitmapFromLruCache(path);

        if(bm != null) {
            refreshBitmap(path, imageView, bm);
        } else {
            addTask(new Runnable() {

                @Override
                public void run() {
                    ImageSize imageSize = getImageViewSize(imageView);
                    Bitmap bm = decodeSampleFromPath(path, imageSize.width, imageSize.height);

                    //图片加入缓存
                    addBitmapToLruCache(path, bm);

                    refreshBitmap(path, imageView, bm);

                    mSemaphoreThreadPool.release();
                }
            });
        }

    }


    private void refreshBitmap(String path, ImageView imageView, Bitmap bm) {
        Message msg = Message.obtain();
        ImgBeanHolder holder = new ImgBeanHolder();
        holder.bitmap = bm;
        holder.path = path;
        holder.imageView = imageView;
        msg.obj = holder;
        mUIHandler.sendMessage(msg);
    }

    /**
     * 图片加入缓存
     *
     * @param path
     * @param bm
     */
    private void addBitmapToLruCache(String path, Bitmap bm) {

        if(getBitmapFromLruCache(path) == null) {
            if(bm != null) {
                mLrucache.put(path, bm);
            }
        }

    }

    private Bitmap decodeSampleFromPath(String path, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calcInSampleSize(options, width, height);

        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return bitmap;
    }

    private int calcInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        int width = options.outWidth;
        int height = options.outHeight;

        int insize = 1;

        if(width > reqWidth || height > reqHeight) {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);

            insize = Math.max(widthRadio, heightRadio);
        }
        return insize;
    }

    private ImageSize getImageViewSize(ImageView imageView) {
        ImageSize imageSize = new ImageSize();

        DisplayMetrics dm = imageView.getContext().getResources().getDisplayMetrics();

        ViewGroup.LayoutParams lp = imageView.getLayoutParams();

        int width = imageView.getWidth();

        if(width <= 0) {
            width = lp.width;
        }
        if(width <= 0) {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
                width = imageView.getMaxWidth();
            else
                width = getImageViewFromField(imageView, "mMaxWidth");
        }
        if(width <= 0) {
            width = dm.widthPixels;
        }


        int heigth = imageView.getHeight();


        if(heigth <= 0) {
            heigth = lp.height;
        }
        if(heigth <= 0) {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
                heigth = imageView.getMaxHeight();
            else
                heigth = getImageViewFromField(imageView, "mMaxHeight");

        }

        if(heigth <= 0) {
            heigth = dm.heightPixels;
        }
        imageSize.width = width;
        imageSize.height = heigth;
        return imageSize;
    }

    private class ImageSize {
        int width;
        int height;
    }


    /**
     *
     */
    private int getImageViewFromField(Object object, String fieldName) {
        int value = 0;

        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);

            int fieldVale = field.getInt(object);
            if(fieldVale > 0 && fieldVale < Integer.MAX_VALUE) {
                value = fieldVale;
            }

        } catch(NoSuchFieldException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }

    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        try {
            if(mPoolThreadHandler == null)
                mSemaphorePoolThreadHandler.acquire();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(110);
    }

    private Bitmap getBitmapFromLruCache(String path) {
        return mLrucache.get(path);
    }

    private class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }

}
