package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

public class MatterReceiverTileEntity extends GenericEnergyHandlerTileEntity {

    public static int MAXENERGY = 100000;
    public static int RECEIVEPERTICK = 500;

    public static final String CMD_SETNAME = "setName";
    public static final String CMD_ADDPLAYER = "addPlayer";
    public static final String CMD_DELPLAYER = "delPlayer";
    public static final String CMD_SETPRIVATE = "setAccess";

    private String name = null;
    private boolean privateAccess = false;
    private Set<String> allowedPlayers = new HashSet<String>();

    public MatterReceiverTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
        TeleportDestination destination = destinations.getDestination(new Coordinate(xCoord, yCoord, zCoord), worldObj.provider.dimensionId);
        if (destination != null) {
            destination.setName(name);
            destinations.save(worldObj);
        }

        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    /**
     * This method is called after putting down a receiver that was earlier wrenched. We need to fix the data in
     * the destination.
     */
    public void updateDestination() {
        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
        TeleportDestination destination = destinations.getDestination(new Coordinate(xCoord, yCoord, zCoord), worldObj.provider.dimensionId);
        if (destination != null) {
            destination.setName(name);
            destinations.save(worldObj);
        }
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public boolean isPrivateAccess() {
        return privateAccess;
    }

    public void setPrivateAccess(boolean privateAccess) {
        this.privateAccess = privateAccess;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public boolean checkAccess(String player) {
        if (!privateAccess) {
            return true;
        }
        return allowedPlayers.contains(player);
    }

    public List<PlayerName> getAllowedPlayers() {
        List<PlayerName> p = new ArrayList<PlayerName>();
        for (String player : allowedPlayers) {
            p.add(new PlayerName(player));
        }
        return p;
    }

    public void addPlayer(String player) {
        if (!allowedPlayers.contains(player)) {
            allowedPlayers.add(player);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public void delPlayer(String player) {
        if (allowedPlayers.contains(player)) {
            allowedPlayers.remove(player);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public int checkStatus() {
        Block block = worldObj.getBlock(xCoord, yCoord+1, zCoord);
        if (!block.isAir(worldObj, xCoord, yCoord+1, zCoord)) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_BLOCKED_MASK;
        }
        block = worldObj.getBlock(xCoord, yCoord+2, zCoord);
        if (!block.isAir(worldObj, xCoord, yCoord+2, zCoord)) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_BLOCKED_MASK;
        }

        if (getEnergyStored(ForgeDirection.DOWN) < MatterTransmitterTileEntity.rfPerTeleportReceiver) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_POWER_LOW_MASK;
        }

        return DialingDeviceTileEntity.DIAL_OK;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        name = tagCompound.getString("tpName");

        privateAccess = tagCompound.getBoolean("private");

        allowedPlayers.clear();
        NBTTagList playerList = tagCompound.getTagList("players", Constants.NBT.TAG_STRING);
        if (playerList != null) {
            for (int i = 0 ; i < playerList.tagCount() ; i++) {
                String player = playerList.getStringTagAt(i);
                allowedPlayers.add(player);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        if (name != null && !name.isEmpty()) {
            tagCompound.setString("tpName", name);
        }

        tagCompound.setBoolean("private", privateAccess);

        NBTTagList playerTagList = new NBTTagList();
        for (String player : allowedPlayers) {
            playerTagList.appendTag(new NBTTagString(player));
        }
        tagCompound.setTag("players", playerTagList);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETNAME.equals(command)) {
            setName(args.get("name").getString());
            return true;
        } else if (CMD_SETPRIVATE.equals(command)) {
            setPrivateAccess(args.get("private").getBoolean());
            return true;
        } else if (CMD_ADDPLAYER.equals(command)) {
            addPlayer(args.get("player").getString());
            return true;
        } else if (CMD_DELPLAYER.equals(command)) {
            delPlayer(args.get("player").getString());
            return true;
        }
        return false;
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        List rc = super.executeWithResultList(command, args);
        if (rc != null) {
            return rc;
        }
        if (MatterTransmitterTileEntity.CMD_GETPLAYERS.equals(command)) {
            return getAllowedPlayers();
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (MatterTransmitterTileEntity.CLIENTCMD_GETPLAYERS.equals(command)) {
            GuiMatterReceiver.storeAllowedPlayersForClient(list);
            return true;
        }
        return false;
    }
}
