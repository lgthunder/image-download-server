package com.example.lib;

import java.io.Serializable;
import java.util.List;

public class Data implements Serializable {
    String folder_name;
    String page;
    String host;
    int size;
    List<String> data;
}
