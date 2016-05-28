package gson8.com.myimageloadertaskqueue.config;

/*
 * MyImageLoader making by Syusuke/琴声悠扬 on 2016/5/28
 * E-Mail: Zyj7810@126.com
 * Package: gson8.com.myimageloader.imgloaderadvance.LoaderConfig
 * Description: null
 */

import android.content.Context;

import java.io.File;

import gson8.com.myimageloadertaskqueue.util.ImageLoaderUtils;


public class LoaderConfig {

    private Context mContext;
    private File diskCacheFolder;


    private int threadCount = 2;
    private int lruCacheSize = 10;
    private int diskLruCacheSize = 10;
    private int taskType = TaskType.TYPE_LIFO;

    private int appVerstion = 1;

    public int getAppVerstion() {
        return appVerstion;
    }

    public LoaderConfig(Context context) {
        this.mContext = context;
        diskCacheFolder = ImageLoaderUtils.getDiskCacheDir(context, null);
        appVerstion = getVersionCode(mContext);
    }


    public LoaderConfig setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public LoaderConfig setLruCacheSize(int lruCacheSize) {
        this.lruCacheSize = lruCacheSize * 1024 * 1024;
        return this;
    }

    public LoaderConfig setDiskLruCacheSize(int liskLruCacheSize) {
        this.diskLruCacheSize = liskLruCacheSize * 1024 * 1024;
        return this;
    }

    public LoaderConfig setDiskCacheFolder(File diskCacheFolder) {
        this.diskCacheFolder = diskCacheFolder;
        return this;
    }

    public LoaderConfig setDiskCacheFolder(String diskCacheFolder) {
        this.diskCacheFolder = new File(diskCacheFolder);
        return this;
    }


    public LoaderConfig setTaskType(int taskType) {
        this.taskType = taskType;
        return this;
    }


    public int getThreadCount() {
        return threadCount;
    }

    public int getLruCacheSize() {
        return lruCacheSize;
    }

    public int getDiskLruCacheSize() {
        return diskLruCacheSize;
    }

    public int getTaskType() {
        return taskType;
    }

    public File getDiskCacheFolder() {
        return diskCacheFolder;
    }


    public int getVersionCode(Context context) {
        try {
            String pkName = context.getPackageName();
            int versionCode = context.getPackageManager()
                    .getPackageInfo(pkName, 0).versionCode;
            return versionCode;
        } catch(Exception e) {
        }
        return 1;
    }

    public class TaskType {
        public static final int TYPE_LIFO = 0;
        public static final int TYPE_FIFO = 1;
    }

}
