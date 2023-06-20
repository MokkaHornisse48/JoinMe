package mod.mh48.joinme.screens;

import mod.mh48.joinme.duckinterface.ServerDuck;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;

public class OpenConnectorScreen extends Screen {

    private static final Text ALLOW_COMMANDS_TEXT = new TranslatableText("selectWorld.allowCommands");
    private static final Text GAME_MODE_TEXT = new TranslatableText("selectWorld.gameMode");
    private static final Text OTHER_PLAYERS_TEXT = new TranslatableText("lanServer.otherPlayers");
    private final Screen parent;
    private GameMode gameMode = GameMode.SURVIVAL;
    private boolean allowCommands;

    public OpenConnectorScreen(Screen parent) {
        super(new TranslatableText("mh48.joinme.menue.openconnector"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(CyclingButtonWidget.builder(GameMode::getSimpleTranslatableName).values((GameMode[])new GameMode[]{GameMode.SURVIVAL, GameMode.SPECTATOR, GameMode.CREATIVE, GameMode.ADVENTURE}).initially(this.gameMode).build(this.width / 2 - 155, 100, 150, 20, GAME_MODE_TEXT, (button, gameMode) -> {
            this.gameMode = gameMode;
        }));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.allowCommands).build(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_TEXT, (button, allowCommands) -> {
            this.allowCommands = allowCommands;
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableText("mh48.joinme.menue.openconnector.open"), button -> {
            /*
            this.client.setScreen(null);
            int i = NetworkUtils.findLocalPort();
            TranslatableText text = this.client.getServer().openToLan(this.gameMode, this.allowCommands, i) ? new TranslatableText("commands.publish.started", i) : new TranslatableText("commands.publish.failed");
            this.client.inGameHud.getChatHud().addMessage(text);
            this.client.updateWindowTitle();*/
            String id = ((ServerDuck) this.client.getServer()).openToJoinMe(this.gameMode, this.allowCommands);
            this.client.inGameHud.getChatHud().addMessage(new TranslatableText("commands.publish.started", id));
            this.client.setScreen(null);
            this.client.updateWindowTitle();
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, this.height - 28, 150, 20, ScreenTexts.CANCEL, button -> this.client.setScreen(this.parent)));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        OpenToLanScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 50, 0xFFFFFF);
        OpenToLanScreen.drawCenteredText(matrices, this.textRenderer, OTHER_PLAYERS_TEXT, this.width / 2, 82, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
