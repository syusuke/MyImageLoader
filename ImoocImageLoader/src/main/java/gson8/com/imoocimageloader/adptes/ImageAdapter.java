package gson8.com.imoocimageloader.adptes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gson8.com.imoocimageloader.R;
import gson8.com.imoocimageloader.util.ImageLoader;

public class ImageAdapter extends BaseAdapter {

    private static Set<String> mSelctImg = new HashSet<>();

    private String mDirPath;
    private List<String> mImagPaths;
    private LayoutInflater mInflater;

    public ImageAdapter(Context context, List<String> mDatas, String dirpath) {
        this.mDirPath = dirpath;
        this.mImagPaths = mDatas;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mImagPaths.size();
    }

    @Override
    public Object getItem(int position) {
        return mImagPaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null) {

            convertView = mInflater.inflate(R.layout.item_gridview, parent, false);

            holder = new ViewHolder();

            holder.img = (ImageView) convertView.findViewById(R.id.id_item_img);
            holder.slectImg = (ImageView) convertView.findViewById(R.id.id_item_select);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.img.setImageResource(R.mipmap.ic_launcher);
        holder.slectImg.setImageResource(R.drawable.no_select);
        holder.img.setColorFilter(null);

        ImageLoader.getInstance(4, ImageLoader.Type.LIFO)
                .loadImage(mDirPath + "/" + mImagPaths.get(position), holder.img);
        final String filePath = mDirPath + "/" + mImagPaths.get(position);

        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //已经被选择
                if(mSelctImg.contains(filePath)) {
                    mSelctImg.remove(filePath);
                    holder.img.setColorFilter(null);
                    holder.slectImg.setImageResource(R.drawable.no_select);
                } else {        //未被选择
                    mSelctImg.add(filePath);
                    holder.img.setColorFilter(Color.parseColor("#77000000"));
                    holder.slectImg.setImageResource(R.drawable.select_pic);
                }

            }
        });

        if(mSelctImg.contains(filePath)) {
            holder.img.setColorFilter(Color.parseColor("#77000000"));
            holder.slectImg.setImageResource(R.drawable.select_pic);
        }
        return convertView;
    }

    class ViewHolder {
        ImageView img;
        ImageView slectImg;
    }

}