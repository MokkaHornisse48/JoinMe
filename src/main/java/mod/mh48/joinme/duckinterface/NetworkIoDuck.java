package mod.mh48.joinme.duckinterface;

import io.netty.channel.ServerChannel;
import net.minecraft.server.ServerNetworkIo;

public interface NetworkIoDuck {
    void bindToJoinMe(ServerNetworkIo serverNetworkIo, ServerChannel address);
}
