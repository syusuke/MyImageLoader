package gson8.com.imoocimageloader;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gson8.com.imoocimageloader.adptes.ImageAdapter;
import gson8.com.imoocimageloader.bean.FolderBean;

public class MainActivity extends AppCompatActivity {


    private GridView mGridView;
    private ImageAdapter mAdapter;
    private RelativeLayout mBottomly;
    private TextView mDirName;
    private TextView mDirCount;

    private ListImgPopupWindows mWindow;

    private List<String> mImgs;

    private File mCurrentDir;
    private int mMaxCount;

    private ProgressDialog mProgressDialog;


    private final int DATA_LOADING = 0x110;

    private List<FolderBean> mFolderBeans = new ArrayList<>();


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == DATA_LOADING) {
                mProgressDialog.dismiss();

                data2View();

                initDirPopupWindow();

            }
        }
    };

    private void initDirPopupWindow() {
        mWindow = new ListImgPopupWindows(this, mFolderBeans);
        mWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lightOn();
            }
        });

        mWindow.setOnDirSelectListener(new ListImgPopupWindows.OnDirSelectListener() {
            @Override
            public void onSelect(FolderBean folderBean) {
                mCurrentDir = new File(folderBean.getDir());
                mImgs = Arrays.asList(mCurrentDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        if(filename.endsWith(".jpg") || filename.endsWith("jpeg") ||
                                filename.endsWith(".png"))
                            return true;
                        return false;
                    }
                }));

                mAdapter =
                        new ImageAdapter(MainActivity.this, mImgs, mCurrentDir.getAbsolutePath());

                mGridView.setAdapter(mAdapter);

                mDirCount.setText(mImgs.size() + "");
                mDirName.setText(folderBean.getName());

                mWindow.dismiss();
            }
        });

    }

    private void lightOn() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }

    private void lightOff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;
        getWindow().setAttributes(lp);
    }


    private void data2View() {


        if(mCurrentDir == null) {
            Toast.makeText(this, "没有扫描到图片", Toast.LENGTH_SHORT).show();
            return;
        }


        mImgs = Arrays.asList(mCurrentDir.list());
        mAdapter = new ImageAdapter(this, mImgs, mCurrentDir.getAbsolutePath());
        mGridView.setAdapter(mAdapter);

        mDirCount.setText(mMaxCount + "");
        mDirName.setText(mCurrentDir.getName());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initEvent();
    }


    private void initEvent() {
        mBottomly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWindow.setAnimationStyle(R.style.Dir_windown_Popup);
                mWindow.showAsDropDown(mBottomly, 0, 0);
                lightOff();
            }
        });

    }

    /**
     * 利用ContextProvider扫描图片
     */
    private void initData() {

        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "存储卡不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");
        new Thread() {
            @Override
            public void run() {
                Uri mImagUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                ContentResolver cr = MainActivity.this.getContentResolver();

                Cursor cursor =
                        cr.query(mImagUri, null, MediaStore.Images.Media.MIME_TYPE + " = ? or " +
                                        MediaStore.Images.Media.MIME_TYPE + " = ? ",
                                new String[]{"image/jpeg", "image/png"},
                                MediaStore.Images.Media.DATE_MODIFIED);
                Set<String> mDirpaths = new HashSet<String>();

                while(cursor.moveToNext()) {

                    String path =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    File parantFile = new File(path).getParentFile();


                    if(parantFile == null)
                        continue;

                    String dirPath = parantFile.getAbsolutePath();

                    FolderBean folderBean = null;
                    if(mDirpaths.contains(dirPath)) {
                        continue;
                    } else {
                        mDirpaths.add(dirPath);

                        folderBean = new FolderBean();
                        folderBean.setDir(dirPath);
                        folderBean.setFirstImgPath(path);

                    }

                    if(parantFile.list() == null)
                        continue;


                    int picSize = parantFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            if(filename.endsWith(".jpg") || filename.endsWith("jpeg") ||
                                    filename.endsWith(".png"))
                                return true;
                            return false;
                        }
                    }).length;

                    folderBean.setCount(picSize);

                    mFolderBeans.add(folderBean);

                    if(picSize > mMaxCount) {
                        mMaxCount = picSize;
                        mCurrentDir = parantFile;
                    }

                }
                cursor.close();


                mHandler.sendEmptyMessage(DATA_LOADING);

            }
        }.start();

    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.id_gridview);
        mBottomly = (RelativeLayout) findViewById(R.id.id_bottom_ly);
        mDirName = (TextView) findViewById(R.id.id_dir_name);
        mDirCount = (TextView) findViewById(R.id.id_dir_count);

    }


}
