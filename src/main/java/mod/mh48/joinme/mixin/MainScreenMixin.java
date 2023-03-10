package mod.mh48.joinme.mixin;

import mod.mh48.joinme.screens.JoinMeConnectorScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.lang.invoke.LambdaMetafactory;
import java.net.URI;

@Mixin(TitleScreen.class)
public abstract class MainScreenMixin extends Screen {
    protected MainScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"),method = "init")
    private void onInit(CallbackInfo ci) {
        int l = this.height / 4 + 48;

        this.addDrawableChild(new ButtonWidget(this.width / 2 + 100 + 4, l + 24 * 1, 50, 20, new TranslatableText("mh48.joinme.menue.test"), button -> {
            this.client.setScreen(new JoinMeConnectorScreen(this));
        }));
    }
}


