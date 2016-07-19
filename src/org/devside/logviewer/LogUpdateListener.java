package org.devside.logviewer;

import java.util.List;

public interface LogUpdateListener {
    void onLogUpdate(List<String> lines);
}
