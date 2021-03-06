package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.network.PacketListFromServer;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class PacketReceiversReady extends PacketListFromServer<PacketReceiversReady,TeleportDestination> {

    public PacketReceiversReady() {
    }

    public PacketReceiversReady(int x, int y, int z, String command, List<TeleportDestination> list) {
        super(x, y, z, command, list);
    }

    @Override
    protected TeleportDestination createItem(ByteBuf buf) {
        return new TeleportDestination(buf);
    }
}
