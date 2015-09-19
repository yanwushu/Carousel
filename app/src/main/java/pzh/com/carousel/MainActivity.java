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
