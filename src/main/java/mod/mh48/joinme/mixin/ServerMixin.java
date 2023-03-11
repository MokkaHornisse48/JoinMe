package mod.mh48.joinme.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import io.netty.channel.local.LocalAddress;
import mod.mh48.joinme.duckinterface.NetworkIoDuck;
import mod.mh48.joinme.duckinterface.ServerDuck;
import mod.mh48.signaling.client.ClientServer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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

import java.net.Proxy;
import java.util.function.Consumer;

@Mixin(IntegratedServer.class)
public abstract class ServerMixin extends MinecraftServer implements ServerDuck {
    @Shadow
    private MinecraftClient client;
    @Shadow
    private GameMode forcedGameMode;
    @Shadow
    private static Logger LOGGER;

    public ClientServer clientServer;

    public ServerMixin(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, @Nullable MinecraftSessionService sessionService, @Nullable GameProfileRepository gameProfileRepo, @Nullable UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
        super(serverThread, session, dataPackManager, saveLoader, proxy, dataFixer, sessionService, gameProfileRepo, userCache, worldGenerationProgressListenerFactory);
    }
    @Unique
    @Override
    public void test() {
        this.sendSystemMessage(Text.of("Hi"),this.getPlayerManager().getPlayerList().get(0).getUuid());
    }

    @Unique
    @Override
    public void openToJoinMe(@Nullable GameMode gameMode, boolean cheatsAllowed, Consumer<String> onId){
        this.client.loadBlockList();
        clientServer = new ClientServer("cool",true);
        LocalAddress address = new LocalAddress("P2PS");
        clientServer.localAddress = address;
        clientServer.onId= onId;
        //this.getNetworkIo().bind(null, port);
        ((NetworkIoDuck)this.getNetworkIo()).bindToJoinMe(this.getNetworkIo(),address);
        new Thread(clientServer).start();
        //LOGGER.info("Started serving on {}", (Object)port);
        this.forcedGameMode = gameMode;
        this.getPlayerManager().setCheatsAllowed(cheatsAllowed);
        int i = this.getPermissionLevel(this.client.player.getGameProfile());
        this.client.player.setClientPermissionLevel(i);
        for (ServerPlayerEntity serverPlayerEntity : this.getPlayerManager().getPlayerList()) {
            this.getCommandManager().sendCommandTree(serverPlayerEntity);
        }
    }

    @Inject(method = "isRemote",at = @At("HEAD"),cancellable = true)
    private void onIsRemote(CallbackInfoReturnable<Boolean> cir){
        if(clientServer != null) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
