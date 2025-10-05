package at.nopro.phasmo;

import at.nopro.phasmo.game.CameraManager;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.listener.preplay.LoginListener;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;

import java.net.InetSocketAddress;

public class MojangAuthWithExceptions {
    public MojangAuthWithExceptions() {
        PacketListenerManager p = MinecraftServer.getPacketListenerManager();

        p.setListener(ConnectionState.LOGIN, ClientLoginStartPacket.class, this::loginStartListener);
    }

    public void loginStartListener(ClientLoginStartPacket packet, PlayerConnection connection) {
        if(packet.profileId().equals(CameraManager.getCamPlayerUUID()) &&
            packet.username().equals(CameraManager.getCamPlayerName())) {

            if(connection.getRemoteAddress() instanceof InetSocketAddress inetSocketAddress) {
                if(!inetSocketAddress.getAddress().isLoopbackAddress()) {
                    connection.kick(Component.text("illegal user"));
                    return;
                }
            }

            GameProfile gameProfile = new GameProfile(
                    CameraManager.getCamPlayerUUID(),
                    CameraManager.getCamPlayerName()
            );

            //LoginListener.enterConfig
            Thread.startVirtualThread(() -> {
                try {
                    GameProfile newGameProfile = MinecraftServer.getConnectionManager().transitionLoginToConfig(connection, gameProfile);
                    if (connection instanceof PlayerSocketConnection socketConnection) {
                        socketConnection.UNSAFE_setProfile(newGameProfile);
                    }
                } catch (Throwable t) {
                    MinecraftServer.getExceptionManager().handleException(t);
                }

            });

            return;
        }
        LoginListener.loginStartListener(packet, connection);
    }
}
