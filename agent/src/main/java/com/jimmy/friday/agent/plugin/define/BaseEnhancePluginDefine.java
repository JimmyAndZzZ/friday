package com.jimmy.friday.agent.plugin.define;

import com.jimmy.friday.agent.base.EnhancePluginDefine;
import com.jimmy.friday.agent.base.StaticMethodsInterceptPoint;
import com.jimmy.friday.agent.bytebuddy.support.WitnessMethod;

import java.util.List;

public abstract class BaseEnhancePluginDefine implements EnhancePluginDefine {

    public String[] witnessClasses() {
        return new String[]{};
    }

    public List<WitnessMethod> witnessMethods() {
        return null;
    }

    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return null;
    }
}
