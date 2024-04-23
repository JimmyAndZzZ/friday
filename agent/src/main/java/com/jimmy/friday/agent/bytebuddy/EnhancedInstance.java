package com.jimmy.friday.agent.bytebuddy;

import com.jimmy.friday.agent.core.EnhancedField;

public interface EnhancedInstance {
    EnhancedField getDynamicField();

    void setDynamicField(EnhancedField value);
}
