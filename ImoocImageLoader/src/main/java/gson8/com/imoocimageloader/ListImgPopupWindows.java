package gson8.com.imoocimageloader;

/*
 * MyImageLoader making by Syusuke/琴声悠扬 on 2016/5/29
 * E-Mail: Zyj7810@126.com
 * Package: gson8.com.imoocimageloader.ListImgPopupWindows
 * Description: null
 */

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import gson8.com.imoocimageloader.bean.FolderBean;
import gson8.com.imoocimageloader.util.ImageLoader;

public class ListImgPopupWindows extends PopupWindow {

    private int mWidth;
    private int mHeight;

    private View mConvertView;

    private ListView mListView;


    private List<FolderBean> mDatas;

    private OnDirSelectListener mOnDirSelectListener;

    public void setOnDirSelectListener(
            OnDirSelectListener mOnDirSelectListener) {
        this.mOnDirSelectListener = mOnDirSelectListener;
    }

    public ListImgPopupWindows(Context context, List<FolderBean> list) {
        calcWidthHeiht(context);
        mConvertView = LayoutInflater.from(context).inflate(R.layout.popup_layout, null);
        mDatas = list;


        setContentView(mConvertView);

        setWidth(mWidth);
        setHeight(mHeight);

        setFocusable(true);
        setTouchable(true);

        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());


        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });


        initView(context);
        initEvent();

    }

    private void initView(Context context) {
        mListView = (ListView) mConvertView.findViewById(R.id.id_list_dir);
        mListView.setAdapter(new ListDirAdapter(context, mDatas));
    }

    private void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mOnDirSelectListener != null)
                    mOnDirSelectListener.onSelect(mDatas.get(position));
            }
        });

    }

    private void calcWidthHeiht(Context context) {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics out = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(out);


        mWidth = out.widthPixels;
        mHeight = (int) (out.heightPixels * 0.7);
    }

    class ListDirAdapter extends ArrayAdapter<FolderBean> {

        private LayoutInflater mInflater;
        private List<FolderBean> mList;

        public ListDirAdapter(Context context, List<FolderBean> objects) {
            super(context, 0, objects);

            mInflater = LayoutInflater.from(context);

        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;


            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.item_popup, parent, false);
                holder = new ViewHolder();
                holder.iv = (ImageView) convertView.findViewById(R.id.id_id_dir_item_img);
                holder.tvName = (TextView) convertView.findViewById(R.id.id_dir_item_name);
                holder.tvCount = (TextView) convertView.findViewById(R.id.id_dir_item_count);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            FolderBean bean = getItem(position);


            //重置
            holder.iv.setImageResource(R.mipmap.ic_launcher);

            ImageLoader.getInstance(4, ImageLoader.Type.LIFO)
                    .loadImage(bean.getFirstImgPath(), holder.iv);


            holder.tvName.setText(bean.getName());
            holder.tvCount.setText(bean.getCount() + "");


            return convertView;
        }

        class ViewHolder {
            ImageView iv;
            TextView tvName;
            TextView tvCount;
        }
    }


    public interface OnDirSelectListener {
        void onSelect(FolderBean folderBean);
    }


}
