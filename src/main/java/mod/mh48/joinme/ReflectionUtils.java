package mod.mh48.joinme;

import io.netty.bootstrap.AbstractBootstrap;

import java.lang.reflect.Method;
import java.nio.channels.Channel;

public class ReflectionUtils {

    public static Method initmethod;

    static {
        try {
            //initmethod = AbstractBootstrap.class.getMethod("init", Channel.class);
            initmethod = AbstractBootstrap.class.getDeclaredMethod("init", io.netty.channel.Channel.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        initmethod.setAccessible(true);
    }
}
