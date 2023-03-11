package mod.mh48.joinme.duckinterface;

import io.netty.channel.local.LocalAddress;
import net.minecraft.server.ServerNetworkIo;

public interface NetworkIoDuck {
    void bindToJoinMe(ServerNetworkIo serverNetworkIo, LocalAddress address);
}
