package com.jimmy.friday.protocol.base;


import com.jimmy.friday.protocol.core.Protocol;

import java.util.Set;

public interface Registered {

    Set<String> getTopics();

    Output registeredClient(Protocol info) throws Exception;

    Input registeredServer(Protocol info, Input input) throws Exception;
}
