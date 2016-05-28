[TOC]

# 图片加载框架

## 1.MyImageLoader+TaskQueue版
`Folder:`** MyImageLoaderTaskQueue**

[源码地址,可直接用的](MyImageLoaderTaskQueue.rar)


用法:
```java

LoaderConfig config;

// this 这里为Context
config = new LoaderConfig(this);

config.setThreadCount(4).setDiskLruCacheSize(100).setLruCacheSize(50).setTaskType(LoaderConfig.TaskType.TYPE_LIFO);

MyImageLoader.getInstance(config).displayImage(imgUrl, ImagerView);



```
* `setThreadCount()`:设置线程池一次运行的个数
* `setDiskLruCacheSize()`: 磁盘缓存大小
* `setLruCacheSize()`:内存缓存大小
* `setTaskType()`:设置任务的顺序模式,有两种模式
	* `TYPE_LIFO`: 最后进来的任务先执行(栈)
	* `TYPE_FIFO`: 先进来的任务先执行(队列)

* `setDiskCacheFolder()`:设置磁盘缓存的路径,可以传入`String`,`File`类型的参数


## 2.MyImageLoader简易版
`Folder `:** App **
```java
 ImageLoader.getInstance(MainActivity.this)
                    .displayImage(holder.iv, list.get(position), null);
```



## 3.加载本地图片版
`Folder`:** ImoocImageLoader **