package mod.mh48.joinme.mixin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;
import mod.mh48.joinme.ReflectionUtils;
import mod.mh48.joinme.duckinterface.NetworkIoDuck;
import net.minecraft.network.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.util.Lazy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Mixin(ServerNetworkIo.class)
public abstract class NetworkIoMixin implements NetworkIoDuck {
    @Shadow
    private static Lazy<NioEventLoopGroup> DEFAULT_CHANNEL;
    @Shadow
    private List<ChannelFuture> channels;
    @Shadow
    private MinecraftServer server;

    @Unique
    @Override
    public void bindToJoinMe(ServerNetworkIo serverNetworkIo, ServerChannel channel){
        List<ChannelFuture> list = this.channels;
        synchronized (list) {
            ServerBootstrap serverBootstrap =new ServerBootstrap();
            NioEventLoopGroup lazy = DEFAULT_CHANNEL.get();
            serverBootstrap.childHandler(new ChannelInitializer<Channel>(){
                @Override
                protected void initChannel(Channel channel) {
                    channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("legacy_query", new LegacyQueryHandler(serverNetworkIo)).addLast("splitter", new SplitterHandler()).addLast("decoder", new DecoderHandler(NetworkSide.SERVERBOUND)).addLast("prepender", new SizePrepender()).addLast("encoder", new PacketEncoder(NetworkSide.CLIENTBOUND));
                    int i = NetworkIoMixin.this.server.getRateLimit();
                    ClientConnection clientConnection = i > 0 ? new RateLimitedConnection(i) : new ClientConnection(NetworkSide.SERVERBOUND);
                    serverNetworkIo.getConnections().add(clientConnection);
                    channel.pipeline().addLast("packet_handler", clientConnection);
                    clientConnection.setPacketListener(new ServerHandshakeNetworkHandler(NetworkIoMixin.this.server, clientConnection));
                }
            }).group(lazy);
            try {
                ReflectionUtils.initmethod.invoke(serverBootstrap,channel);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            this.channels.add(serverBootstrap.config().group().register(channel));

        }
        /*
        );*/
    }
}
