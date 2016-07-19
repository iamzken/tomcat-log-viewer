package org.devside.logviewer;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.Calendar;

public class LogFileWatcher extends Thread {
    protected static final Logger log = Logger.getLogger(LogFileWatcher.class);
    private Vector<LogUpdateListener> listeners = new Vector<LogUpdateListener>();
    private File file;
    private int interval = 1;
    private int numLines = 100;
    private BufferedReader reader;
    private boolean active = false;
    private Vector<String> updateLines = new Vector<String>();

    public LogFileWatcher(String filename) throws IOException {
        this(new File(filename), 1, 100);
    }

    public LogFileWatcher(String filename, int interval, int numLines) throws IOException {
        this(new File(filename), interval, numLines);
    }

    public LogFileWatcher(File file, int interval, int numLines) throws IOException {
        this.file = file;
        this.interval = interval;
        this.numLines = numLines;
        this.reader = new BufferedReader(new FileReader(file));
    }

    /**
     * 终止掉LogFileWatcher的执行
     */
    public void halt() {
        this.active = false;
    }

    /**
     * 加入一个更新监听器
     *
     * @param listener 更新监听器
     */
    public void addListener(LogUpdateListener listener) {
        listeners.add(listener);
    }

    /**
     * 向每个更新监听器通告的行数
     */
    protected synchronized void notifyListeners() {
        for (LogUpdateListener listener : listeners) {
            listener.onLogUpdate(this.updateLines);
        }
    }

    public void clear() {
        updateLines.clear();
        notifyListeners();
    }

    public String getFilename() {
        if (file != null) {
            return file.getName();
        } else {
            return null;
        }
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getNumLines() {
        return numLines;
    }

    public void setNumLines(int numLines) {
        if (numLines == 0) {
            this.numLines = Integer.MAX_VALUE;
        } else {
            this.numLines = numLines;
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public void run() {
        this.active = true;
        updateLines = new Vector<String>();
        String line;
        long size = 0;
        while (active) {
            // 持续的检查文件中的新行
            boolean updated = false;
            boolean truncated = false;

            // 看看这个文件有没有被删减
            if (file.length() < size) {
                truncated = true;
            }
            size = file.length();
            try {
                //如果文件被删减过了,重置文件流
                if (truncated) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(file.lastModified());
                    updateLines.add("---- 文件被删减, 时间: " + cal.getTime() + " ----");
                    updated = true;

                    reader.close();
                    reader = new BufferedReader(new FileReader(file));

                    // 重置文件流
                    while (reader.readLine() != null) {
                    }
                } else if (!file.exists()) {
                    //如果文件不存在了
                    Calendar cal = Calendar.getInstance();
                    updateLines.add("---- 文件被删除, 时间: " + cal.getTime() + " ----");
                    updated = true;
                    active = false;
                } else {
                    // 不断的从文件里读行信息
                    while ((line = reader.readLine()) != null) {
                        if (line.length() > 0) {
                            updated = true;
                            updateLines.add(line);
                        }
                    }
                }


                if (updated) {
                    notifyListeners();
                }
                sleep(this.interval * 1000);
                updateLines.clear();
            } catch (IOException ex) {
                log.warn(ex);
            } catch (InterruptedException ex) {
                log.warn(ex);
            }
        }
    }
}
