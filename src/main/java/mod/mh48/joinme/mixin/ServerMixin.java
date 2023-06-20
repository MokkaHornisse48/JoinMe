package mod.mh48.joinme.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import mod.mh48.joinme.duckinterface.NetworkIoDuck;
import mod.mh48.joinme.duckinterface.ServerDuck;
import mod.mh48.p2p48.P2PServerSocket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;
import net.minecraft.world.GameMode;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;

@Mixin(IntegratedServer.class)
public abstract class ServerMixin extends MinecraftServer implements ServerDuck {
    @Shadow
    private MinecraftClient client;
    @Shadow
    private GameMode forcedGameMode;
    @Shadow
    private static Logger LOGGER;

    public P2PServerSocket p2pServerSocket;

    public ServerMixin(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, @Nullable MinecraftSessionService sessionService, @Nullable GameProfileRepository gameProfileRepo, @Nullable UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
        super(serverThread, session, dataPackManager, saveLoader, proxy, dataFixer, sessionService, gameProfileRepo, userCache, worldGenerationProgressListenerFactory);
    }

    @Unique
    @Override
    public String openToJoinMe(@Nullable GameMode gameMode, boolean cheatsAllowed){
        this.client.loadBlockList();
        try {
            p2pServerSocket = new P2PServerSocket(new InetSocketAddress("185.213.25.234",12001),new URI("ws://185.213.25.234:27776"),"test", true);//todo config JoinMe.instance.config.signalingServer
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        ServerChannel serverChannel = new OioServerSocketChannel(p2pServerSocket);

        ((NetworkIoDuck)this.getNetworkIo()).bindToJoinMe(this.getNetworkIo(),serverChannel);
        //LOGGER.info("Started serving on {}", (Object)port);
        this.forcedGameMode = gameMode;
        this.getPlayerManager().setCheatsAllowed(cheatsAllowed);
        int i = this.getPermissionLevel(this.client.player.getGameProfile());
        this.client.player.setClientPermissionLevel(i);
        for (ServerPlayerEntity serverPlayerEntity : this.getPlayerManager().getPlayerList()) {
            this.getCommandManager().sendCommandTree(serverPlayerEntity);
        }
        return p2pServerSocket.getId();
    }

    @Inject(method = "isRemote",at = @At("HEAD"),cancellable = true)
    private void onIsRemote(CallbackInfoReturnable<Boolean> cir){
        if(p2pServerSocket != null) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
