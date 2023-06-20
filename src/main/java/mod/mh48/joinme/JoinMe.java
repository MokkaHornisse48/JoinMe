package mod.mh48.joinme;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;



public class JoinMe implements ModInitializer {
    public JoinMeConfig config = new JoinMeConfig();
    public static JoinMe instance;

    public JoinMe(){
        instance = this;
    }
    @Override
    public void onInitialize() {

    }
}
