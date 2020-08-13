package com.example.lib;

import java.io.Serializable;
import java.util.List;

public class Data implements Serializable {
    String folder_name;
    String page;
    String host;
    int size;
    int state;//0 init;1 need reload;2 complete
    List<String> data;
}
