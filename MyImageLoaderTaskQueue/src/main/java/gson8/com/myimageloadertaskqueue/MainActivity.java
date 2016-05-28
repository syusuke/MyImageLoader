package gson8.com.myimageloadertaskqueue;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import gson8.com.myimageloadertaskqueue.config.LoaderConfig;
import gson8.com.myimageloadertaskqueue.loader.MyImageLoader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerView rv;
    Button load;
    Adapters adapters;

    List<String> list;


    LoaderConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = (RecyclerView) findViewById(R.id.rv);
        load = (Button) findViewById(R.id.load);
        list = new ArrayList<>();

        config = new LoaderConfig(this);

        config.setThreadCount(4).setDiskLruCacheSize(100).setLruCacheSize(50).setTaskType(
                LoaderConfig.TaskType.TYPE_LIFO);
        load.setOnClickListener(this);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapters = new Adapters();
        rv.setAdapter(adapters);

    }

    @Override
    public void onClick(View v) {
        initData();
        adapters.notifyDataSetChanged();
    }


    class Adapters extends RecyclerView.Adapter<Adapters.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.item_img, parent, false));
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {

            MyImageLoader.getInstance(config).displayImage(list.get(position), holder.iv);

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView iv;

            public VH(View itemView) {
                super(itemView);
                iv = (ImageView) itemView.findViewById(R.id.iv1);
            }
        }
    }

    public void initData() {
        list.add("http://tnfs.tngou.net/img/ext/160408/e65ae3efd6d166c3c88e1679c7d5a383.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160408/8f0efad281dc3081149fb15dd98cc5f1.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160408/2f1c0bc4e32f1b8fa791b11128f55271.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160408/2cfc1a42f861905d7f6543757703b867.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160408/a24ba76cf7195dc5a121d559f92703ac.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160408/ec1412bfdb4d6d563c4e874f830295b7.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160408/47dd6128603b8109de6a2fc2cca35b1f.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160408/7776120f5cc0bc42ba62668ed83797be.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160408/20027675037e4f827214809980960462.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160408/781d21b6c58520f4b95973a8e22b1ead.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160225/75bc8fc642c7053edac19706a7018d12.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160225/344cf35ee84d533f93d3f770a0785cd1.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160225/90cc804b0c96416db21232e5dc144fd9.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160225/222502ccbb54e91c18169a9e33f654f5.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160225/1a6a6413104c2ea0ab7ed38d31a07444.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160225/bb1f065e23276d4cd4d7cd74b187f077.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160225/e11c74779cab97db9eb4e583bb282ca9.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160225/55d0ac8344b8cdf61705bbfc5eb39acb.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160225/3ac1be43f5c1cebd06ac20b98cf314ca.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160225/0dc5fa0233fda7ef1edeb047b129b3c7.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/9f77927365e2fc34f895bfe373dc2af2.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/ee8b1a91d84c25a2183e90b2100df97d.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/d360fe7723891d01e01cd1a25984d883.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/626b822d866b7e9e1bacbb4cb1a5da9c.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/a834a9e7f0f0b0da1bc935422331b40d.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/f796e85bad9703862a611597b237897f.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/51e0119de3d829e22df28c1a61a9a118.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/5980119433ef21b42e40358e26a6ede3.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/08ad3011425df09f50c2486ddbffcc9b.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/08ad3011425df09f50c2486ddbffcc9b.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/8467fbf001365edd109a08672fbf9eec.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/11aa1491cf5613838b7771726460c186.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/88f8716ba24c9d8173a64e9abb33bb03.jpg");
        list.add("http://tnfs.tngou.net/img/ext/160417/be3f9b7d20f5997bdd6bb1ad8ad65e25.jpg");
    }

}
