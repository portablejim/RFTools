package com.mcjty.rftools.items.teleportprobe;

import com.mcjty.rftools.blocks.teleporter.TeleportDestination;
import com.mcjty.rftools.blocks.teleporter.TeleportDestinations;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class PacketGetAllReceivers implements IMessage, IMessageHandler<PacketGetAllReceivers, PacketAllReceiversReady> {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketGetAllReceivers() {
    }

    @Override
    public PacketAllReceiversReady onMessage(PacketGetAllReceivers message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        TeleportDestinations destinations = TeleportDestinations.getDestinations(player.worldObj);
        List<TeleportDestination> destinationList = new ArrayList<TeleportDestination> (destinations.getValidDestinations());
        return new PacketAllReceiversReady(destinationList);
    }

}
