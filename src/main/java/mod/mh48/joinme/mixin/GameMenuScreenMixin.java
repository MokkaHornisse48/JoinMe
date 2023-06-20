package mod.mh48.joinme.mixin;

import mod.mh48.joinme.screens.JoinMeConnectorScreen;
import mod.mh48.joinme.screens.OpenConnectorScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"),method = "init")
    private void onInit(CallbackInfo ci) {
        ButtonWidget buttonWidget = this.addDrawableChild(
                new ButtonWidget(this.width / 2 + 4 + 102, this.height / 4 + 96 + -16, 98/2, 20, new TranslatableText("mh48.joinme.menue.publish"),
                        button ->
                                this.client.setScreen(new OpenConnectorScreen(this))
                ));
    }
}
