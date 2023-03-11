package mod.mh48.joinme.duckinterface;

import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface ServerDuck {



    void test();


    void openToJoinMe(@Nullable GameMode gameMode, boolean cheatsAllowed, Consumer<String> onId);
}
