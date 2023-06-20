package mod.mh48.joinme.screens;

import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import mod.mh48.joinme.ReflectionUtils;
import mod.mh48.joinme.mixin.MinecraftClientAccessor;
import mod.mh48.p2p48.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.*;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.*;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

public class P2PConnectScreen
        extends Screen {
    private static final AtomicInteger CONNECTOR_THREADS_COUNT = new AtomicInteger(0);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final long NARRATOR_INTERVAL = 2000L;
    public static final Text BLOCKED_HOST_TEXT = new TranslatableText("disconnect.genericReason", new TranslatableText("disconnect.unknownHost"));

    @Nullable
    volatile ClientConnection connection;
    volatile boolean connectingCancelled;
    final Screen parent;
    private Text status = new TranslatableText("connect.connecting");
    private long lastNarrationTime = -1L;

    private P2PConnectScreen(Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
    }

    public static void connect(Screen screen, MinecraftClient client, String id) {
        P2PConnectScreen connectScreen = new P2PConnectScreen(screen);
        client.disconnect();
        client.loadBlockList();
        client.setCurrentServerEntry(new ServerInfo("Send", "127.0.0.1", false));
        client.setScreen(connectScreen);
        connectScreen.connect(client,id);
    }

    private void connect(final MinecraftClient client, String id) {
        Thread thread = new Thread("Server Connector #" + CONNECTOR_THREADS_COUNT.incrementAndGet()){

            @Override
            public void run() {
                try {
                    if (P2PConnectScreen.this.connectingCancelled) {
                        return;
                    }
                    System.out.println("con 0");
                    Socket socket = Utils.connectToServer(new InetSocketAddress("185.213.25.234",12001),new URI("ws://185.213.25.234:27776"),id);
                    System.out.println("con 1");
                    if (P2PConnectScreen.this.connectingCancelled) {
                        return;
                    }
                    P2PConnectScreen.this.connection = P2PConnectScreen.connect(new OioSocketChannel(socket));
                    ((MinecraftClientAccessor)P2PConnectScreen.this.client).setConnection(P2PConnectScreen.this.connection);
                    System.out.println("con 2");
                    P2PConnectScreen.this.connection.setPacketListener(new ClientLoginNetworkHandler(P2PConnectScreen.this.connection, client, P2PConnectScreen.this.parent, P2PConnectScreen.this::setStatus));
                    System.out.println("con 3");
                    P2PConnectScreen.this.connection.send(new HandshakeC2SPacket("127.0.0.1", 25565, NetworkState.LOGIN));
                    System.out.println("con 4");
                    P2PConnectScreen.this.connection.send(new LoginHelloC2SPacket(client.getSession().getProfile()));
                    System.out.println("con 5");
                } catch (Exception exception) {
                    if (P2PConnectScreen.this.connectingCancelled) {
                        return;
                    }
                    LOGGER.error("Couldn't connect to server", exception);
                }
            }
        };
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
        thread.start();
    }

    private void setStatus(Text status) {
        this.status = status;
    }

    @Override
    public void tick() {
        if (this.connection != null) {
            if (this.connection.isOpen()) {
                this.connection.tick();
            } else {
                this.connection.handleDisconnection();
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, ScreenTexts.CANCEL, button -> {
            this.connectingCancelled = true;
            if (this.connection != null) {
                this.connection.disconnect(new TranslatableText("connect.aborted"));
            }
            this.client.setScreen(this.parent);
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        long l = Util.getMeasuringTimeMs();
        if (l - this.lastNarrationTime > 2000L) {
            this.lastNarrationTime = l;
            NarratorManager.INSTANCE.narrate(new TranslatableText("narrator.joining"));
        }
        this.drawCenteredText(matrices, this.textRenderer, this.status, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public static ClientConnection connect(Channel channel) {
        NioEventLoopGroup lazy = ClientConnection.CLIENT_IO_GROUP.get();
        final ClientConnection clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(lazy).handler(new ChannelInitializer<Channel>(){

            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30)).addLast("splitter", (ChannelHandler)new SplitterHandler()).addLast("decoder", (ChannelHandler)new DecoderHandler(NetworkSide.CLIENTBOUND)).addLast("prepender", (ChannelHandler)new SizePrepender()).addLast("encoder", (ChannelHandler)new PacketEncoder(NetworkSide.SERVERBOUND)).addLast("packet_handler", (ChannelHandler)clientConnection);
            }
        }).channel(LocalChannel.class);
        //((AbstractBootstrapAccessor)bootstrap).init(channel);
        try {
            ReflectionUtils.initmethod.invoke(bootstrap,channel);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        bootstrap.config().group().register(channel);
        return clientConnection;
    }
}
