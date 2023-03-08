package top.ncserver.chatimg.Tools;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import java.util.Map;

public class Img {
    Map<Integer, String> packages=new HashMap<>();
    int packageNum=0;
    int width=64;
    int height=64;
    public Img(int packageNum,int index,String data) {
        this.packageNum=packageNum;
        packages.put(index,data);
    }
    public void setWidthAndHeight(int width,int height) {

        BigDecimal b = new BigDecimal((float) height / width);
        double hx = b.setScale(2, RoundingMode.HALF_UP).doubleValue();
        if (width > 300) {
            this.width = 300;
            this.height = (int) (300 * hx);
        }
        if (height > 96) {
            this.height = 96;
            this.width = (int) (96 / hx);
        }
        while ((this.width > 300 ||this.height > 96)&&this.height>16){
            this.height--;
            this.width = (int) (this.height/ hx);
        }

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void add(int index, String data) {
        if (!packages.containsKey(index))
            packages.put(index,data);
        else {
            packages.remove(index);
            packages.put(index,data);
        }
    }
    public boolean allReceived(){
        return packages.size() == packageNum;
    }
    public String getData(){
        StringBuilder sb=new StringBuilder();
        if (allReceived()) {
            for (int i = 0; i < packageNum; i++) {
                try {
                    sb.append(packages.get(i));
                } catch (Exception e) {
                   return "";
                }
            }
            return sb.toString();
        }
        return "";
    }

}
