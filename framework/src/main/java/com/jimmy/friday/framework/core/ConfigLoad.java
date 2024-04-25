package com.jimmy.friday.framework.core;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.ConfigConstants;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.framework.utils.DockerUtil;
import io.netty.util.internal.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class ConfigLoad {

    private static final String DEFAULT_ID = IdUtil.simpleUUID();

    private String path;

    private Properties properties;

    private InetAddress inetAddress;

    @Setter
    @Getter
    private Set<String> gatewayPackagesToScan;

    @Setter
    @Getter
    private Set<String> schedulePackagesToScan;

    public ConfigLoad() {
        String property = System.getProperty("gateway.config.path");
        this.path = StringUtil.isNullOrEmpty(property) ? "gateway.properties" : property;
        this.loadProperties(path);

        String name = this.getApplicationName();
        if (StrUtil.isEmpty(name)) {
            throw new GatewayException("未配置应用名");
        }

        this.inetAddress = this.findFirstNonLoopBackAddress();
    }

    public InetAddress getLocalIpAddress() {
        return inetAddress;
    }

    public String getApplicationName() {
        return this.get(ConfigConstants.APPLICATION_NAME);
    }

    public String getOffsetFilePath() {
        return StrUtil.emptyToDefault(this.get(ConfigConstants.OFFSET_PATH), GlobalConstants.Client.DEFAULT_OFFSET_PATH);
    }

    public String get(String key, String defaultValue) {
        return StrUtil.emptyToDefault(this.get(key), defaultValue);
    }

    public String get(String key) {
        // 读取配置
        return properties != null ? properties.getProperty(key) : null;
    }

    public String getId() {
        String taskSlot = DockerUtil.getTaskSlot();
        if (StrUtil.isNotEmpty(taskSlot)) {
            return this.getApplicationName() + "-" + taskSlot;
        }

        String s = this.get(ConfigConstants.ID);
        if (StrUtil.isEmpty(s)) {
            try (OutputStream output = Files.newOutputStream(Paths.get(path))) {
                properties.setProperty(ConfigConstants.ID, DEFAULT_ID);
                // 将属性写入到属性文件
                properties.store(output, null);
            } catch (IOException e) {
                throw new GatewayException("properties 写入失败");
            }
        }

        return this.get(ConfigConstants.ID);
    }

    public String getVersion() {
        return StrUtil.emptyToDefault(this.get(ConfigConstants.VERSION), GlobalConstants.DEFAULT_VERSION);
    }

    public Integer getWeight() {
        return Convert.toInt(this.get(ConfigConstants.WEIGHT), 0);
    }

    private void loadProperties(String filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath); InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream); BufferedReader bfReader = new BufferedReader(inputStreamReader)) {

            this.properties = new Properties();
            properties.load(bfReader);
        } catch (IOException e) {
            throw new GatewayException("配置文件读取失败");
        }
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    private InetAddress findFirstNonLoopBackAddress() {
        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else {
                        continue;
                    }

                    if (!ignoreNetWorkInterface(ifc.getDisplayName())) {
                        for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements(); ) {
                            InetAddress address = addrs.nextElement();
                            if (address instanceof Inet4Address && !address.isLoopbackAddress() && isPreferredAddress(address)) {
                                result = address;
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            log.error("Cannot get first non-loopback address", ex);
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.warn("Unable to retrieve localhost");
        }

        return null;
    }


    /**
     * 首选ip地址判断
     *
     * @param address
     * @return
     */
    private boolean isPreferredAddress(InetAddress address) {
        String s = this.get(ConfigConstants.PREFERRED_NETWORKS);
        if (StrUtil.isEmpty(s)) {
            return true;
        }

        List<String> preferredNetworks = StrUtil.split(s, ",");
        for (String regex : preferredNetworks) {
            final String hostAddress = address.getHostAddress();
            if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 网络接口过滤
     *
     * @param interfaceName
     * @return
     */
    private boolean ignoreNetWorkInterface(String interfaceName) {
        String s = this.get(ConfigConstants.IGNORED_NETWORK_INTERFACES);
        if (StrUtil.isNotEmpty(s)) {
            List<String> split = StrUtil.split(s, ",");
            for (String regex : split) {
                if (interfaceName.matches(regex)) {
                    return true;
                }
            }
        }

        return false;
    }
}
