package me.draimgoose.draimshop.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.plugin.Plugin;

public class ProtocolLibHandler extends PacketAdapter {
    private BlockDamageEvent evt;

    public ProtocolLibHandler(Plugin plugin, PacketType type, BlockDamageEvent evt) {
        super(plugin, type);
        this.evt = evt;
    }

    @Override
    public void onPacketReceiving(PacketEvent e) {
        PacketContainer packet = e.getPacket();
        PlayerDigType digType = packet.getPlayerDigTypes().getValues().get(0);
        if (digType.name().equals("ABORT_DESTROY_BLOCK")) {
            this.evt.setCancelled(true);
        }
    }
}
