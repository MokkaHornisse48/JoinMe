package mod.mh48.joinme.duckinterface;

import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Consumer;

public interface ServerDuck {

    @Unique
    String openToJoinMe(@Nullable GameMode gameMode, boolean cheatsAllowed);
}
