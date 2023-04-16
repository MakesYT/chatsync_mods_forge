package top.ncserver.chatimg.Tools;

public class ImgJsonSTC {
    public int id;
    public String base64imgdata = "base64imgdata";
    public int packageNum;
    public int index;
    public String data;
    public String sender;

    public ImgJsonSTC(int id, int packageNum, String sender) {
        this.id = id;
        this.packageNum = packageNum;
        this.sender = sender;
    }

    public void setData(int index, String data) {
        this.index = index;
        this.data = data;
    }
}
