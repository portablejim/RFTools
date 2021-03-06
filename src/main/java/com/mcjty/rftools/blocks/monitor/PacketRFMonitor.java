package com.mcjty.rftools.blocks.monitor;

import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketRFMonitor implements IMessage, IMessageHandler<PacketRFMonitor, IMessage> {
    private int x;
    private int y;
    private int z;

    private Coordinate monitor;

    private int alarmLevel;
    private RFMonitorMode alarmMode;

    public PacketRFMonitor() {
        monitor = new Coordinate(-1, -1, -1);
        alarmLevel = -1;
        alarmMode = RFMonitorMode.MODE_OFF;
    }

    public PacketRFMonitor(int x, int y, int z, Coordinate monitor) {
        this();
        this.x = x;
        this.y = y;
        this.z = z;
        this.monitor = monitor;
    }

    public PacketRFMonitor(int x, int y, int z, RFMonitorMode alarmMode, int alarmLevel) {
        this();
        this.x = x;
        this.y = y;
        this.z = z;
        this.alarmLevel = alarmLevel;
        this.alarmMode = alarmMode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        monitor = new Coordinate(buf.readInt(), buf.readInt(), buf.readInt());
        alarmLevel = buf.readByte();
        alarmMode = RFMonitorMode.getModeFromIndex(buf.readByte());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(monitor.getX());
        buf.writeInt(monitor.getY());
        buf.writeInt(monitor.getZ());
        buf.writeByte(alarmLevel);
        buf.writeByte(alarmMode.getIndex());
    }

    @Override
    public IMessage onMessage(PacketRFMonitor message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof RFMonitorBlockTileEntity)) {
            // @Todo better logging
            System.out.println("TileEntity is not a RFMonitorBlockTileEntity!");
            return null;
        }
        RFMonitorBlockTileEntity monitorBlockTileEntity = (RFMonitorBlockTileEntity) te;
        if (message.monitor.getY() != -1) {
            monitorBlockTileEntity.setMonitor(message.monitor);
        }
        if (message.alarmLevel != -1) {
            monitorBlockTileEntity.setAlarm(message.alarmMode, message.alarmLevel);
        }
        return null;
    }
}
