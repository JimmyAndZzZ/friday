package com.jimmy.friday.agent.core;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.jimmy.friday.agent.other.AntPathMatcher;
import com.jimmy.friday.agent.utils.DockerUtil;
import com.jimmy.friday.boot.core.agent.Topology;
import com.jimmy.friday.boot.enums.agent.TopologyTypeEnum;
import com.jimmy.friday.boot.other.ConfigConstants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class ConfigLoad {

    private Properties properties;

    private static ConfigLoad configLoad;

    private ConfigLoad() {
        String property = System.getProperty("friday.config.path");
        loadProperties(Strings.isNullOrEmpty(property) ? "friday.properties" : property);
    }

    public static ConfigLoad getDefault() {
        if (configLoad == null) {
            synchronized (ConfigLoad.class) {
                if (configLoad == null) {
                    configLoad = new ConfigLoad();
                }
            }
        }
        return configLoad;
    }

    public Topology getTopology() {
        String ip = configLoad.get(ConfigConstants.ADDRESS);
        String applicationName = this.getApplicationName();

        Topology topology = new Topology();
        topology.setType(TopologyTypeEnum.APPLICATION.getCode());
        topology.setApplication(applicationName);
        topology.setMachine(ip);
        return topology;
    }

    public String getApplicationName() {
        String applicationName = configLoad.get(ConfigConstants.APPLICATION_NAME);

        String taskSlot = DockerUtil.getTaskSlot();
        if (StrUtil.isNotEmpty(taskSlot)) {
            applicationName = applicationName + "-" + taskSlot;
        }

        return applicationName;
    }

    public boolean isExistProperties() {
        return properties != null;
    }

    public Set<String> getCollectorPath() {
        return this.getCollector(ConfigConstants.COLLECTOR_PATH);
    }

    public Set<String> getIgnorePath() {
        return this.getCollector(ConfigConstants.IGNORE_COLLECTOR_PATH);
    }

    public boolean httpUrlIsMatch(String url) {
        String s = this.get(ConfigConstants.HTTP_ALL_PUSH);
        if (Strings.isNullOrEmpty(s)) {
            return true;
        }

        if (Boolean.valueOf(s)) {
            return true;
        }

        Set<String> collector = this.getCollector(ConfigConstants.MATCH_HTTP_URL);
        if (collector.isEmpty()) {
            return false;
        }

        AntPathMatcher matcher = new AntPathMatcher();
        for (String ss : collector) {
            if (collector.contains(collector) || matcher.match(ss, url)) {
                return true;
            }
        }

        return false;
    }

    public Set<String> getIgnoreClass() {
        return this.getCollector(ConfigConstants.IGNORE_COLLECTOR_CLASS);
    }

    public boolean logLevelIsMatch(String levelStr) {
        Set<String> levels = new HashSet<>();

        String logLevel = this.get(ConfigConstants.LOG_PUSH_LEVEL);
        if (!Strings.isNullOrEmpty(logLevel)) {
            levels.addAll(Arrays.asList(logLevel.toUpperCase().split(",")));
        }

        return levels.isEmpty() || levels.contains(levelStr);
    }

    public boolean logPointIsMatch(String classPoint, String methodPoint) {
        if (logAllPush()) {
            return true;
        }

        Map<String, List<String>> point = ConfigLoad.getDefault().getPoint(ConfigConstants.LOG_COLLECTOR_POINT);
        if (!point.containsKey(classPoint)) {
            return false;
        }

        List<String> list = point.get(classPoint);
        return list != null && !list.isEmpty() && list.contains(methodPoint);
    }

    public boolean qpsPointIsMatch(String classPoint, String methodPoint) {
        if (qpsAllPush()) {
            return true;
        }

        Map<String, List<String>> point = ConfigLoad.getDefault().getPoint(ConfigConstants.QPS_COLLECTOR_POINT);
        if (!point.containsKey(classPoint)) {
            return false;
        }

        List<String> list = point.get(classPoint);
        return list != null && !list.isEmpty() && list.contains(methodPoint);
    }

    public boolean logAllPush() {
        String s = this.get(ConfigConstants.LOG_ALL_PUSH);
        return Strings.isNullOrEmpty(s) ? false : Boolean.valueOf(s);
    }

    public boolean qpsAllPush() {
        String s = this.get(ConfigConstants.QPS_ALL_PUSH);
        return Strings.isNullOrEmpty(s) ? false : Boolean.valueOf(s);
    }

    public boolean isMatch(String actualName) {
        Set<String> point = this.getCollectorPath();
        if (point.isEmpty()) {
            return false;
        }

        Set<String> ignorePath = this.getIgnorePath();
        if (!ignorePath.isEmpty()) {
            for (String s : ignorePath) {
                if (actualName.startsWith(s)) {
                    return false;
                }
            }
        }

        Set<String> ignoreClass = this.getIgnoreClass();
        if (!ignoreClass.isEmpty()) {
            for (String s : ignoreClass) {
                if (actualName.equalsIgnoreCase(s)) {
                    return false;
                }
            }
        }

        for (String s : point) {
            if (actualName.startsWith(s)) {
                return true;
            }
        }

        return false;
    }

    public Map<String, List<String>> getPoint(String pointKey) {
        Map<String, List<String>> conf = new HashMap<>();

        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(pointKey)) {
                String value = this.get(key);
                if (value != null && !value.isEmpty()) {
                    String[] split = value.split(",");
                    String prefix = key.substring(pointKey.length());
                    conf.put(prefix, Arrays.asList(split));
                }
            }
        }

        return conf;
    }

    public String get(String key) {
        // 读取配置
        return properties != null ? properties.getProperty(key) : null;
    }

    private Set<String> getCollector(String key) {
        Set<String> paths = Sets.newHashSet();

        String collectorPath = this.get(key);
        if (Strings.isNullOrEmpty(collectorPath)) {
            return paths;
        }

        Iterable<String> parts = Splitter.on(",").split(collectorPath);
        for (String part : parts) {
            paths.add(part);
        }

        return paths;
    }

    /**
     * 读取配置
     *
     * @param filePath
     * @return
     */
    private void loadProperties(String filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
             BufferedReader bfReader = new BufferedReader(inputStreamReader)) {

            this.properties = new Properties();
            properties.load(bfReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
