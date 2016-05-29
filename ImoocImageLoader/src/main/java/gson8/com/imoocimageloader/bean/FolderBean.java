package gson8.com.imoocimageloader.bean;

/*
 * MyImageLoader making by Syusuke/琴声悠扬 on 2016/5/29
 * E-Mail: Zyj7810@126.com
 * Package: gson8.com.imoocimageloader.bean.FolderBean
 * Description: null
 */

public class FolderBean {

    private String dir;
    private String firstImgPath;
    private String name;
    private int count;


    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexof = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexof + 1);
    }

    public String getFirstImgPath() {
        return firstImgPath;
    }

    public void setFirstImgPath(String firstImgPath) {
        this.firstImgPath = firstImgPath;
    }

    public String getName() {
        return name;
    }


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
