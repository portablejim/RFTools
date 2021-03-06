package com.mcjty.rftools.blocks.monitor;

import com.mcjty.container.GenericBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class RFMonitorBlock extends GenericBlock {
    private IIcon iconFront0;
    private IIcon iconFront1;
    private IIcon iconFront2;
    private IIcon iconFront3;
    private IIcon iconFront4;

    public RFMonitorBlock(Material material) {
        super(material, RFMonitorBlockTileEntity.class);
        setBlockName("rfMonitorBlock");
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineFront";
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_RF_MONITOR;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        super.registerBlockIcons(iconRegister);
        iconFront0 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront_0");
        iconFront1 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront_1");
        iconFront2 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront_2");
        iconFront3 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront_3");
        iconFront4 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront_4");
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int metadata = blockAccess.getBlockMetadata(x, y, z);
        return BlockTools.getRedstoneSignal(metadata) ? 15 : 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int metadata = blockAccess.getBlockMetadata(x, y, z);
        return BlockTools.getRedstoneSignal(metadata) ? 15 : 0;
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
        int meta = blockAccess.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientation(meta);
        if (side == k.ordinal()) {
            RFMonitorBlockTileEntity monitorBlockTileEntity = (RFMonitorBlockTileEntity) tileEntity;
            int rflevel = monitorBlockTileEntity.getRflevel();
            switch (rflevel) {
                case 1: return iconFront0;
                case 2: return iconFront1;
                case 3: return iconFront2;
                case 4: return iconFront3;
                case 5: return iconFront4;
                default: return iconInd;

            }
        } else {
            return iconSide;
        }
    }
}
