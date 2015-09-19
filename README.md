# Carousel
一个图片轮播控件
效果如如下
![效果图](http://7xlwwd.com1.z0.glb.clouddn.com/yanwushuS50919-220901.jpg)

#特性

- 此组件继承自linearLayout，意味着可以随意嵌入到任何布局文件中
- 轮播的帧数(组件下方点数量)随着提供数据量动态指定
- 使用startup方法启动此组件，使用shutdown方法停止此组件的轮播动作
- 使用回调函数指定点击之后的处理逻辑
- 包括一个使用案例，代码如下：

##activity布局文件
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    android:scrollbars="none">

    <LinearLayout
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        android:background="#FFFFFF"
        android:orientation="vertical">

        <pzh.com.carousel.Carousel
            android:id="@+id/crs"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content" />
    </LinearLayout>
</ScrollView>
```

##activity代码
```java
package pzh.com.carousel;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private Carousel c;
    private List<CarouselData> data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControl();
        //开启轮播控件
        data = new ArrayList<CarouselData>();
        String[] urls = new String[]{
                "http://7xlwwd.com1.z0.glb.clouddn.com/yanwushu1.jpg",
                "http://7xlwwd.com1.z0.glb.clouddn.com/yanwushu2.jpg",
                "http://7xlwwd.com1.z0.glb.clouddn.com/yanwushu3.jpg"
        };
        for (int i = 0; i < urls.length; i++) {
            CarouselData d = new CarouselData();
            d.setImage(urls[i]);
            d.setTitle("测试tile" + i);
            d.setId(i);
            data.add(d);
        }
        c.startup(data);
    }

    private void initControl() {
        c = (Carousel) findViewById(R.id.crs);
        c.setCallback(new Carousel.ClickCallback() {
            @Override
            public void perform(int id, int position) {
                Toast.makeText(MainActivity.this, "id:" + id + "position" + position + "title:" + data.get(position).getTitle(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        c.shutdown();
    }
}
```

#PS

- 环境为android studio
- 使用`com.nostra13.universalimageloader:universal-image-loader:1.8.6`实现图片下载
