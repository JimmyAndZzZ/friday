package com.jimmy.friday.center.support;

import cn.hutool.core.util.ArrayUtil;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.utils.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

@Component
public class ActionSupport implements Initialize {

    private final Map<EventTypeEnum, Action<?>> actionMap = Maps.newHashMap();

    private final Map<EventTypeEnum, Class<?>> classMap = Maps.newHashMap();

    @Autowired
    private ApplicationContext applicationContext;

    public void action(Event event, ChannelHandlerContext ctx) {
        String type = event.getType();
        String message = event.getMessage();

        EventTypeEnum eventTypeEnum = EventTypeEnum.queryByCode(type);
        if (eventTypeEnum == null) {
            return;
        }

        Class<?> clazz = classMap.get(eventTypeEnum);
        Action action = actionMap.get(eventTypeEnum);

        if (action == null || clazz == null) {
            return;
        }

        action.action(JsonUtil.parseObject(message, clazz), ctx);
    }

    @Override
    public void init() throws Exception {
        Map<String, Action> beansOfType = applicationContext.getBeansOfType(Action.class);
        for (Action value : beansOfType.values()) {
            EventTypeEnum type = value.type();
            actionMap.put(type, value);

            Type[] genericInterfaces = value.getClass().getGenericInterfaces();
            if (ArrayUtil.isNotEmpty(genericInterfaces)) {
                Type genericInterface = genericInterfaces[0];
                // 如果gType类型是ParameterizedType对象
                if (genericInterface instanceof ParameterizedType) {
                    // 强制类型转换
                    ParameterizedType pType = (ParameterizedType) genericInterface;
                    // 取得泛型类型的泛型参数
                    Type[] tArgs = pType.getActualTypeArguments();
                    if (ArrayUtil.isNotEmpty(tArgs)) {
                        classMap.put(type, Class.forName(tArgs[0].getTypeName()));
                    }
                }
            }

        }
    }

    @Override
    public int sort() {
        return 0;
    }
}
