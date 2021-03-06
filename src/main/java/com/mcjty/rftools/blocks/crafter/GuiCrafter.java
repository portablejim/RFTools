package com.mcjty.rftools.blocks.crafter;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.events.DefaultSelectionEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketServerCommand;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;

public class GuiCrafter extends GuiContainer {
    public static final int CRAFTER_WIDTH = 256;
    public static final int CRAFTER_HEIGHT = 224;

    private Window window;
    private EnergyBar energyBar;
    private WidgetList recipeList;
    private ChoiceLabel keepItem;
    private ChoiceLabel internalRecipe;
    private Button applyButton;
    private ImageChoiceLabel redstoneMode;
    private ImageChoiceLabel speedMode;

    private final CrafterBlockTileEntity3 crafterBlockTileEntity;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/crafter.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiCrafter(CrafterBlockTileEntity3 crafterBlockTileEntity, CrafterContainer container) {
        super(container);
        this.crafterBlockTileEntity = crafterBlockTileEntity;
        crafterBlockTileEntity.setOldRF(-1);
        crafterBlockTileEntity.setCurrentRF(crafterBlockTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = CRAFTER_WIDTH;
        ySize = CRAFTER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = crafterBlockTileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(12, 141, 8, 76)).setShowText(false);
        energyBar.setValue(crafterBlockTileEntity.getCurrentRF());

        keepItem = new ChoiceLabel(mc, this).
                addChoices("All", "Keep").
                setTooltips("'Keep' will keep one", "item in every inventory", "slot").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        updateRecipe();
                    }
                }).
                setEnabled(false).
                setLayoutHint(new PositionalLayout.PositionalHint(150, 7, 38, 14));
        internalRecipe = new ChoiceLabel(mc, this).
                addChoices("Ext", "Int").
                setTooltips("'Int' will put result of", "crafting operation in", "inventory instead of", "output buffer").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        updateRecipe();
                    }
                }).
                setEnabled(false).
                setLayoutHint(new PositionalLayout.PositionalHint(150, 24, 38, 14));

        recipeList = new WidgetList(mc, this).
                setFilledRectThickness(1).
                addSelectionEvent(new DefaultSelectionEvent() {
                    @Override
                    public void select(Widget parent, int index) {
                        selectRecipe();
                    }
                }).
                setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 125, 80));
        populateList();

        Slider listSlider = new Slider(mc, this).setVertical().setScrollable(recipeList).setLayoutHint(new PositionalLayout.PositionalHint(137, 7, 11, 80));
        applyButton = new Button(mc, this).
                setText("Apply").
                setTooltips("Press to apply the", "recipe to the crafter").
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        applyRecipe();
                    }
                }).
                setEnabled(false).
                setLayoutHint(new PositionalLayout.PositionalHint(212, 65, 34, 16));

        redstoneMode = new ImageChoiceLabel(mc, this).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeRedstoneMode();
                    }
                }).
                addChoice("Ignored", "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                addChoice("Off", "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                addChoice("On", "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setLayoutHint(new PositionalLayout.PositionalHint(31, 186, 16, 16));
        redstoneMode.setCurrentChoice(crafterBlockTileEntity.getRedstoneMode());

        speedMode = new ImageChoiceLabel(mc, this).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeSpeedMode();
                    }
                }).
                addChoice("Slow", "Speed mode:\nSlow", iconGuiElements, 48, 0).
                addChoice("Fast", "Speed mode:\nFast", iconGuiElements, 64, 0);
        speedMode.setLayoutHint(new PositionalLayout.PositionalHint(49, 186, 16, 16));
        speedMode.setCurrentChoice(crafterBlockTileEntity.getSpeedMode());

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).addChild(keepItem).addChild(internalRecipe).
                addChild(recipeList).addChild(listSlider).addChild(applyButton).addChild(redstoneMode).addChild(speedMode);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        selectRecipe();
        sendChangeToServer(-1, null, null, false, false);

        window = new Window(this, toplevel);
    }

    private void changeRedstoneMode() {
        crafterBlockTileEntity.setRedstoneMode(redstoneMode.getCurrentChoice());
        sendChangeToServer();
    }

    private void changeSpeedMode() {
        crafterBlockTileEntity.setSpeedMode(speedMode.getCurrentChoice());
        sendChangeToServer();
    }

    private void sendChangeToServer() {
        int rsMode = redstoneMode.getCurrentChoice();
        int sMode = speedMode.getCurrentChoice();
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(crafterBlockTileEntity.xCoord, crafterBlockTileEntity.yCoord, crafterBlockTileEntity.zCoord,
                CrafterBlockTileEntity3.CMD_MODE,
                new Argument("rs", rsMode), new Argument("speed", sMode)));
    }

    private void populateList() {
        recipeList.removeChildren();
        for (int i = 0 ; i < crafterBlockTileEntity.getSupportedRecipes() ; i++) {
            CraftingRecipe recipe = crafterBlockTileEntity.getRecipe(i);
            ItemStack stack = recipe.getResult();
            addRecipeLine(stack);
        }
    }

    private void addRecipeLine(Object craftingResult) {
        Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout()).
                addChild(new BlockRender(mc, this).setRenderItem(craftingResult)).
                addChild(new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDynamic(true).setText(BlockInfo.getReadableName(craftingResult, 0)));
        recipeList.addChild(panel);
    }

    private void selectRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            for (int i = 0 ; i < 10 ; i++) {
                inventorySlots.getSlot(i).putStack(null);
            }
            keepItem.setChoice("All");
            internalRecipe.setChoice("Ext");
            keepItem.setEnabled(false);
            internalRecipe.setEnabled(false);
            applyButton.setEnabled(false);
            return;
        }
        CraftingRecipe craftingRecipe = crafterBlockTileEntity.getRecipe(selected);
        InventoryCrafting inv = craftingRecipe.getInventory();
        for (int i = 0 ; i < 9 ; i++) {
            inventorySlots.getSlot(i).putStack(inv.getStackInSlot(i));
        }
        inventorySlots.getSlot(9).putStack(craftingRecipe.getResult());
        keepItem.setChoice(craftingRecipe.isKeepOne() ? "Keep" : "All");
        internalRecipe.setChoice(craftingRecipe.isCraftInternal() ? "Int" : "Ext");
        keepItem.setEnabled(true);
        internalRecipe.setEnabled(true);
        applyButton.setEnabled(true);
    }

    private void testRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }

        CraftingRecipe craftingRecipe = crafterBlockTileEntity.getRecipe(selected);
        InventoryCrafting inv = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer var1) {
                return false;
            }
        }, 3, 3);

        for (int i = 0 ; i < 9 ; i++) {
            inv.setInventorySlotContents(i, inventorySlots.getSlot(i).getStack());
        }

        // Compare current contents to avoid unneeded slot update.
        IRecipe recipe = CraftingRecipe.findRecipe(mc.theWorld, inv);
        ItemStack newResult;
        if (recipe == null) {
            newResult = null;
        } else {
            newResult = recipe.getCraftingResult(inv);
        }
        inventorySlots.getSlot(9).putStack(newResult);
    }

    private void applyRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }

        CraftingRecipe craftingRecipe = crafterBlockTileEntity.getRecipe(selected);
        InventoryCrafting inv = craftingRecipe.getInventory();

        for (int i = 0 ; i < 9 ; i++) {
            ItemStack oldStack = inv.getStackInSlot(i);
            ItemStack newStack = inventorySlots.getSlot(i).getStack();
            if (!itemStacksEqual(oldStack, newStack)) {
                inv.setInventorySlotContents(i, newStack);
            }
        }

        // Compare current contents to avoid unneeded slot update.
        IRecipe recipe = CraftingRecipe.findRecipe(mc.theWorld, inv);
        ItemStack newResult;
        if (recipe == null) {
            newResult = null;
        } else {
            newResult = recipe.getCraftingResult(inv);
        }
        ItemStack oldResult = inventorySlots.getSlot(9).getStack();
        if (!itemStacksEqual(oldResult, newResult)) {
            inventorySlots.getSlot(9).putStack(newResult);
        }

        craftingRecipe.setResult(newResult);
        updateRecipe();
        populateList();
    }

    private void updateRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }
        CraftingRecipe craftingRecipe = crafterBlockTileEntity.getRecipe(selected);
        boolean keepOne = "Keep".equals(keepItem.getCurrentChoice());
        boolean craftInternal = "Int".equals(internalRecipe.getCurrentChoice());
        craftingRecipe.setKeepOne(keepOne);
        craftingRecipe.setCraftInternal(craftInternal);
        sendChangeToServer(selected, craftingRecipe.getInventory(), craftingRecipe.getResult(), keepOne, craftInternal);
    }

    private boolean itemStacksEqual(ItemStack matches, ItemStack oldStack) {
        if (matches == null) {
            return oldStack == null;
        } else if (oldStack == null) {
            return false;
        } else {
            return matches.isItemEqual(oldStack);
        }
    }

    private void sendChangeToServer(int index, InventoryCrafting inv, ItemStack result, boolean keepOne, boolean craftInternal) {
        PacketHandler.INSTANCE.sendToServer(new PacketCrafter(crafterBlockTileEntity.xCoord, crafterBlockTileEntity.yCoord, crafterBlockTileEntity.zCoord, index, inv,
                result, keepOne, craftInternal));
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3) {
        super.drawScreen(par1, par2, par3);
        testRecipe();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
        int currentRF = crafterBlockTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        window.handleMouseInput();
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        window.mouseMovedOrUp(x, y, button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }

}
