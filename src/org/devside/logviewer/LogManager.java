package org.devside.logviewer;

import org.apache.log4j.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class LogManager {
    protected static final Logger log = Logger.getLogger(LogManager.class);

    private LogFileWatcher watcher;

    private final Vector<Filter> filters = new Vector<Filter>();

    /**
     * 停止监控
     */
    public void stop() {
        if (watcher != null) {
            watcher.halt();
        }
    }

    /**
     * 发送log信息
     */
    public void send(String filename) {
        WebContext wctx = WebContextFactory.get();
        final ScriptSession scriptSession = wctx.getScriptSession();
        if (watcher != null) {
            watcher.halt();
        }

        try {
            watcher = new LogFileWatcher(filename);
            watcher.addListener(new LogUpdateListener() {
                public void onLogUpdate(List<String> lines) {
                    for (String line : lines) {
                        if (checkFilters(line)) {
                            ScriptBuffer scriptBuffer = new ScriptBuffer();
                            scriptBuffer.appendScript("addNewLine(")
                                    .appendData(line)
                                    .appendScript(");");
                            scriptSession.addScript(scriptBuffer);
                        }
                    }
                }
            });
            watcher.start();
        } catch (IOException e) {
            ScriptBuffer scriptBuffer = new ScriptBuffer();
            scriptBuffer.appendScript("addNewLine(")
                    .appendData(e.getMessage())
                    .appendScript(");");
            scriptSession.addScript(scriptBuffer);
            log.warn(e);
        }
    }

    /**
     * 用filter 过滤这一行,现在的Filter比较简单<br>
     * 规则如下:<br>
     * <li> 如果filters里没有过滤器,通过
     * <li> 如果filters有过滤器,任何一个过滤器通过,则此行通过
     *
     * @param line
     * @return true: 通过; false 不通过
     */
    private boolean checkFilters(String line) {
        if (filters.size() == 0) {
            return true;
        } else {
            for (Filter filter : filters) {
                if (filter.isMatch(line)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 取得指定的日志文件路径
     *
     * @return 指定的日志文件路径
     */
    public List<String> getLogFileNames() {
        List<String> filenames = new ArrayList<String>();
        try {
            XMLConfiguration config = getConfiguration();
            List logfiles = config.getList("log-files.file");
            for (Object o : logfiles) {
                filenames.add((String) o);
            }
        } catch (ConfigurationException e) {
            log.warn(e);
        }

        return filenames;
    }

    /**
     * 取得指定的日志目录下的文件
     *
     * @return 指定的日志目录下的文件
     */
    public List<String> getLogFileNamesFromDir() {
        List<String> filenames = new ArrayList<String>();
        try {
            XMLConfiguration config = getConfiguration();
            String dir = config.getString("log-dir.dir");
            if (dir != null) {
                File rootDir = new File(dir);
                if (rootDir.exists()) {
                    if (rootDir.isFile()) {
                        filenames.add(rootDir.getPath().replace('\\', '/'));
                    } else if (rootDir.isDirectory()) {
                        String patternString = config.getString("log-dir.filter");
                        File[] files;
                        if (patternString != null && !patternString.equals("")) {
                            files = rootDir.listFiles(new LogFileFilter(patternString));
                        } else {
                            files = rootDir.listFiles();
                        }

                        for (File file : files) {
                            filenames.add(file.getPath().replace('\\', '/'));
                        }
                    }
                }
            }
        } catch (ConfigurationException e) {
            log.warn(e);
        }

        return filenames;
    }

    public int addFilter(String regex) {
        synchronized (filters) {
            Filter filter = new Filter(regex, SequenceGenerator.getInstance().next(), FilterType.INCLUDE);
            filters.add(filter);
            return filter.getId();
        }

    }

    public void removeFilter(int id) {
        synchronized (filters) {
            filters.remove(new Filter(id));
        }
    }

    public List<Map<String, Object>> getFilters() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        synchronized (filters) {
            for (Filter filter : filters) {
                Map<String, Object> filterItem = new HashMap<String, Object>();
                filterItem.put("id", filter.getId());
                filterItem.put("pattern", filter.getPattern().pattern());
                result.add(filterItem);
            }
        }
        return result;
    }

    /**
     * 获得 配置
     *
     * @return XMLConfiguration
     * @throws ConfigurationException
     */
    private XMLConfiguration getConfiguration() throws ConfigurationException {
        WebContext webCtx = WebContextFactory.get();
        String realPath = webCtx.getServletContext().getRealPath("");
        String confPath = realPath + "/WEB-INF/conf.xml";
        return new XMLConfiguration(confPath);
    }

    /**
     * 基于正则表达式的文件名过滤器
     */
    class LogFileFilter implements FilenameFilter {
        private Pattern pattern;

        public LogFileFilter(String patternString) {
            this.pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        }

        public boolean accept(File dir, String name) {
            Matcher m = pattern.matcher(name);
            return m.find();
        }
    }
}
