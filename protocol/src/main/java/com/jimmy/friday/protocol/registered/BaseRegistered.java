package com.jimmy.friday.protocol.registered;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.protocol.annotations.RegisteredType;
import com.jimmy.friday.protocol.base.Input;
import com.jimmy.friday.protocol.base.Output;
import com.jimmy.friday.protocol.base.Registered;
import com.jimmy.friday.protocol.config.ProtocolProperty;
import com.jimmy.friday.protocol.core.Protocol;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class BaseRegistered implements Registered {

    protected static final String REG_CONFIG = "^\\$\\{(.*?)\\}$";

    protected Set<String> topics = Sets.newHashSet();

    protected Set<String> inputKeys = Sets.newHashSet();

    protected Map<String, Output> beanNames = Maps.newHashMap();

    protected Environment environment;

    protected DefaultListableBeanFactory beanFactory;

    public void init(ProtocolProperty protocolProperty) {

    }

    public void close() {

    }

    public void close(Protocol info) {

    }

    @Override
    public Set<String> getTopics() {
        return this.topics;
    }

    @Override
    public Output registeredClient(Protocol info) throws Exception {
        topics.add(info.getTopic());
        String key = outputKeyInit(info);
        if (!beanNames.containsKey(key)) {
            beanNames.put(key, outputGene(info));
        }

        return beanNames.get(key);
    }

    @Override
    public Input registeredServer(Protocol info, Input input) throws Exception {
        topics.add(info.getTopic());
        String key = inputKeyInit(info);

        if (!inputKeys.add(key)) {
            throw new IllegalArgumentException("该监听器已存在,key:" + key);
        }

        return inputGene(info, input);
    }

    public abstract Input inputGene(Protocol info, Input input);

    public abstract Output outputGene(Protocol info);

    public String outputKeyInit(Protocol info) {
        return type().getMessage() + ":" + info.getTopic() + "Output";
    }

    public String inputKeyInit(Protocol info) {
        return type().getMessage() + ":" + info.getTopic() + "Input";
    }

    public void setBeanFactory(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public ProtocolEnum type() {
        RegisteredType annotation = AnnotationUtils.getAnnotation(this.getClass(), RegisteredType.class);
        if (annotation == null) {
            return null;
        }

        return annotation.type();
    }
}
