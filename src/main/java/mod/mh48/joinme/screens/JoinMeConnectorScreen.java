package mod.mh48.joinme.screens;


import net.minecraft.client.gui.screen.DirectConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class JoinMeConnectorScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget connectorIdField;
    private ButtonWidget connectButton;

    public JoinMeConnectorScreen(Screen parent) {
        super(new TranslatableText("mh48.joinme.menue.connector"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.client.keyboard.setRepeatEvents(true);
        this.connectorIdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 66, 200, 20, new TranslatableText("addServer.enterName"));
        this.connectorIdField.setTextFieldFocused(true);
        //this.connectorIdField.setText(this.server.name);
        this.connectorIdField.setChangedListener(serverName -> {

                }
        );
        this.addSelectableChild(this.connectorIdField);
        this.connectButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20, new TranslatableText("selectServer.select"), button -> {

            MultiplayerScreen multiplayerScreen = new MultiplayerScreen(parent);
            P2PConnectScreen.connect(parent,this.client,this.connectorIdField.getText());
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20, ScreenTexts.CANCEL, button -> {
            this.client.setScreen(this.parent);
        }));
        this.setInitialFocus(connectorIdField);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        DirectConnectScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        //DirectConnectScreen.drawTextWithShadow(matrices, this.textRenderer, ENTER_IP_TEXT, this.width / 2 - 100, 100, 0xA0A0A0);
        this.connectorIdField.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
