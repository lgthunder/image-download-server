package com.example.lib;

import java.io.Serializable;

public class ImgData implements Serializable {
    public ImgData(String l, int c) {
        url = l;
        count = c;
    }

    String url;
    int count;


}
