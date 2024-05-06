package com.jimmy.friday.center.base.gateway;

import java.util.List;

public interface File {

    List<String> suffix();

    int getPage(java.io.File file);
}
