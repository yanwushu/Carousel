package pzh.com.carousel;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by peng on 2015/9/19.
 * 图片轮播控件
 * 特性及使用方式如下：
 * 1.此组件extends linearLayout，意味着可以随意嵌入到任何布局文件中
 * 2.轮播的帧数(组件下方点数量)随着提供数据量(adlist.getSize())动态指定
 * 3.使用startup(List<AdList>)方法启动此组件，使用shutdown方法停止此组件的轮播动作
 * 4.使用回调函数指定点击之后的处理逻辑
 */
public class Carousel extends LinearLayout {

    //默认图片缓存路径，可以通过setter进行设置，默认如下值
    private String imageCachePath = "imageloader/Cache";

    //region fileds
    private Context context;
    private ViewPager viewPager;
    private List<ImageView> imageViews;// 滑动的图片集合
    private List<View> dots; //标识当前图片的点
    private TextView tv_title;
    private int currentItem = 0; // 当前图片的索引
    private LinearLayout dotWraper;
    private ScheduledExecutorService scheduledExecutorService;

    // 异步加载图片
    private ImageLoader imgLoader;
    private DisplayImageOptions options;

    //轮播数据源
    private List<CarouselData> data;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            viewPager.setCurrentItem(currentItem);
        }
    };

    private ClickCallback callback;
    //endregion

    public Carousel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.carousel, this, true);
    }

    private void initImageLoader() {
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.bg)
                .showImageForEmptyUri(R.drawable.bg)
                .showImageOnFail(R.drawable.bg)
                .cacheInMemory(true).cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY).build();
        imgLoader = ImageLoader.getInstance();
        File cacheDir = com.nostra13.universalimageloader.utils.StorageUtils
                .getOwnCacheDirectory(context.getApplicationContext(),
                        imageCachePath);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisc(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new LruMemoryCache(12 * 1024 * 1024))
                .memoryCacheSize(12 * 1024 * 1024)
                .discCacheSize(32 * 1024 * 1024).discCacheFileCount(100)
                .discCache(new UnlimitedDiscCache(cacheDir))
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();
        imgLoader.init(config);
    }

    private void initControl() {
        initImageLoader();
        imageViews = new ArrayList<ImageView>();
        tv_title = (TextView) findViewById(R.id.tv_title);
        viewPager = (ViewPager) findViewById(R.id.vp);
    }

    private void addDynamicView() {
        // 动态添加图片和下面指示的圆点
        // 初始化图片资源
        if (null == data || data.size() < 1)
            return;
        for (int i = 0; i < data.size(); i++) {
            ImageView imageView = new ImageView(context);
            // 异步加载图片
            imgLoader.displayImage(data.get(i).getImage(), imageView,
                    options);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViews.add(imageView);
            dots.get(i).setVisibility(View.VISIBLE);
        }
    }

    private void start() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        // 每两秒切换一次图片显示
        scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 1, 2,
                TimeUnit.SECONDS);
    }

    private class ScrollTask implements Runnable {
        @Override
        public void run() {
            synchronized (viewPager) {
                currentItem = (currentItem + 1) % imageViews.size();
                handler.obtainMessage().sendToTarget();
            }
        }
    }

    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {

        private int oldPosition = 0;

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int position) {
            currentItem = position;
            if (null == data || data.size() < 1)
                return;
            CarouselData ad = data.get(position);
            tv_title.setText(ad.getTitle());
            dots.get(oldPosition).setBackgroundResource(R.drawable.dot_normal);
            dots.get(position).setBackgroundResource(R.drawable.dot_focused);
            oldPosition = position;
        }
    }

    private class MyPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            ImageView iv = imageViews.get(position);
            ((ViewPager) container).addView(iv);
            final CarouselData dataItem = data.get(position);
            iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) {
                        callback.perform(dataItem.getId(), position);
                    }
                }
            });
            return iv;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    //启动
    public void startup(List<CarouselData> adList) {
        initControl();
        this.data = adList;
        dotWraper = (LinearLayout) findViewById(R.id.dot_wraper);
        dots = new ArrayList<View>();
        for (int i = 0; i < adList.size(); i++) {
            View dot = LayoutInflater.from(context).inflate(R.layout.carousel_dot, this, false);
            dots.add(dot);
            dotWraper.addView(dot);
        }
        addDynamicView();
        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.setOnPageChangeListener(new MyPageChangeListener());
        start();
    }

    //停止此控件的轮播
    public void shutdown() {
        if (scheduledExecutorService != null)
            scheduledExecutorService.shutdown();
    }

    interface ClickCallback {
        public void perform(int id, int position);
    }

    //region getter and setter
    public String getImageCachePath() {
        return imageCachePath;
    }

    public void setImageCachePath(String imageCachePath) {
        this.imageCachePath = imageCachePath;
    }

    public void setCallback(ClickCallback callback) {
        this.callback = callback;
    }
    //endregion
}
