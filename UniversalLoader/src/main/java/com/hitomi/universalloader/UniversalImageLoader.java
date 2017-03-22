package com.hitomi.universalloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

/**
 * Created by hitomi on 2017/5/3.
 */
public class UniversalImageLoader implements com.hitomi.tilibrary.loader.ImageLoader {
    private Context context;

    private UniversalImageLoader(Context context) {
        this.context = context;
        initImageLoader(context);
    }

    public static UniversalImageLoader with(Context context) {
        return new UniversalImageLoader(context);
    }

    private void initImageLoader(Context context) {
        int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() / 5);
        MemoryCache memoryCache;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            memoryCache = new LruMemoryCache(memoryCacheSize);
        } else {
            memoryCache = new LRULimitedMemoryCache(memoryCacheSize);
        }

        DisplayImageOptions normalImageOptions = new DisplayImageOptions
                .Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .build();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration
                .Builder(context)
                .defaultDisplayImageOptions(normalImageOptions)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(memoryCache)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .threadPoolSize(3)
                .build();

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(configuration);
    }

    @Override
    public void showSourceImage(String srcUrl, ImageView imageView, Drawable placeholder, final SourceCallback sourceCallback) {
        DisplayImageOptions options = new DisplayImageOptions
                .Builder()
                .showImageOnLoading(placeholder)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .build();

        ImageLoader.getInstance().displayImage(srcUrl, imageView, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                sourceCallback.onStart();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                sourceCallback.onDelivered(STATUS_DISPLAY_FAILED);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                sourceCallback.onFinish();
                sourceCallback.onDelivered(STATUS_DISPLAY_SUCCESS);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                sourceCallback.onDelivered(STATUS_DISPLAY_CANCEL);
            }
        }, new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                sourceCallback.onProgress(current * 100 / total);
            }
        });
    }

    @Override
    public void loadThumbnailAsync(String thumbUrl, ImageView imageView, final ThumbnailCallback callback) {
        ImageLoader.getInstance().loadImage(thumbUrl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                callback.onFinish(new BitmapDrawable(context.getResources(), loadedImage));
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });
    }
}
