package fr.darkbow_.speedrunnervshunter;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Titles {
    public void sendTitle(final Player player, final String title, final String subtitle, final int ticks) {
        final IChatBaseComponent basetitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}");
        final IChatBaseComponent basesubtitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");
        final PacketPlayOutTitle titlepacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, basetitle);
        final PacketPlayOutTitle subtitlepacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, basesubtitle);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket((Packet)titlepacket);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket((Packet)subtitlepacket);
        this.sendTime(player, ticks);
    }

    private void sendTime(final Player player, final int ticks) {
        final PacketPlayOutTitle titlepacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, (IChatBaseComponent)null, 20, ticks, 20);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket((Packet)titlepacket);
    }

    public void sendActionBar(final Player player, final String message) {
        final IChatBaseComponent basetitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        final PacketPlayOutChat packet = new PacketPlayOutChat(basetitle, ChatMessageType.GAME_INFO, player.getUniqueId());
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket((Packet)packet);
    }
}
