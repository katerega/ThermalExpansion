package thermalexpansion.item;

import cofh.api.energy.IEnergyContainerItem;
import cofh.core.item.ItemBase;
import cofh.core.util.CoreUtils;
import cofh.lib.util.helpers.EnergyHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.StringHelper;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import thermalexpansion.ThermalExpansion;

public class ItemCapacitor extends ItemBase implements IEnergyContainerItem {

	public ItemCapacitor() {

		super("thermalexpansion");
		setMaxDamage(1);
		setMaxStackSize(1);
		setCreativeTab(ThermalExpansion.tabTools);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {

		list.add(EnergyHelper.setDefaultEnergyTag(new ItemStack(item, 1, Types.CREATIVE.ordinal()), STORAGE[Types.CREATIVE.ordinal()]));
		list.add(EnergyHelper.setDefaultEnergyTag(new ItemStack(item, 1, Types.POTATO.ordinal()), STORAGE[Types.POTATO.ordinal()]));
		for (int i = 2; i < Types.values().length; i++) {
			list.add(EnergyHelper.setDefaultEnergyTag(new ItemStack(item, 1, i), 0));
			list.add(EnergyHelper.setDefaultEnergyTag(new ItemStack(item, 1, i), STORAGE[i]));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {

		return "item.thermalexpansion.capacitor." + NAMES[ItemHelper.getItemDamage(item)];
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean check) {

		if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
			list.add(StringHelper.shiftForDetails());
		}
		if (stack.stackTagCompound == null) {
			EnergyHelper.setDefaultEnergyTag(stack, 0);
		}
		if (!StringHelper.isShiftKeyDown()) {
			return;
		}
		if (ItemHelper.getItemDamage(stack) == Types.CREATIVE.ordinal()) {
			list.add(StringHelper.localize("info.cofh.charge") + ": 1.21G RF");
		} else {
			list.add(StringHelper.localize("info.cofh.charge") + ": " + StringHelper.getScaledNumber(stack.stackTagCompound.getInteger("Energy")) + " / "
					+ StringHelper.getScaledNumber(STORAGE[ItemHelper.getItemDamage(stack)]) + " RF");
		}
		list.add(StringHelper.localize("info.cofh.send") + "/" + StringHelper.localize("info.cofh.receive") + ": " + SEND[ItemHelper.getItemDamage(stack)]
					+ "/" + RECEIVE[ItemHelper.getItemDamage(stack)] + " RF/t");
		if (isActive(stack)) {
			list.add(StringHelper.getInfoText("info.thermalexpansion.capacitor.2"));
			list.add(StringHelper.getInfoText("info.thermalexpansion.capacitor.4"));
			list.add(StringHelper.getDeactivationText("info.thermalexpansion.capacitor.3"));
		} else {
			list.add(StringHelper.getInfoText("info.thermalexpansion.capacitor.0"));
			list.add(StringHelper.getInfoText("info.thermalexpansion.capacitor.4"));
			list.add(StringHelper.getActivationText("info.thermalexpansion.capacitor.1"));
		}
		if (ItemHelper.getItemDamage(stack) == Types.POTATO.ordinal()) {
			list.add(StringHelper.getFlavorText("info.thermalexpansion.capacitor.potato"));
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrentItem) {

		if (slot > 8 || !isActive(stack) || isCurrentItem) {
			return;
		}
		InventoryPlayer playerInv = ((EntityPlayer) entity).inventory;
		IEnergyContainerItem containerItem;
		int toSend = Math.min(getEnergyStored(stack), SEND[ItemHelper.getItemDamage(stack)]);

		ItemStack currentItem = playerInv.getCurrentItem();

		if (EnergyHelper.isEnergyContainerItem(currentItem)) {
			containerItem = (IEnergyContainerItem) currentItem.getItem();
			extractEnergy(stack, containerItem.receiveEnergy(currentItem, toSend, false), false);
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if (CoreUtils.isFakePlayer(player)) {
			return stack;
		}
		if (player.isSneaking()) {
			if (setActiveState(stack, !isActive(stack))) {
				if (isActive(stack)) {
					player.worldObj.playSoundAtEntity(player, "random.orb", 0.2F, 0.8F);
				} else {
					player.worldObj.playSoundAtEntity(player, "random.orb", 0.2F, 0.5F);
				}
			}
		}
		player.swingItem();
		return stack;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int hitSide, float hitX, float hitY, float hitZ) {

		return false;
	}

	@Override
	public boolean hasEffect(ItemStack stack) {

		return isActive(stack);
	}

	@Override
	public boolean isFull3D() {

		return true;
	}

	@Override
	public boolean isItemTool(ItemStack stack) {

		return false;
	}

	@Override
	public boolean isDamaged(ItemStack stack) {

		return ItemHelper.getItemDamage(stack) != Types.CREATIVE.ordinal();
	}

	@Override
	public int getDisplayDamage(ItemStack stack) {

		if (stack.stackTagCompound == null) {
			return STORAGE[ItemHelper.getItemDamage(stack)];
		}
		return STORAGE[ItemHelper.getItemDamage(stack)] - stack.stackTagCompound.getInteger("Energy");
	}

	@Override
	public int getMaxDamage(ItemStack stack) {

		return STORAGE[ItemHelper.getItemDamage(stack)];
	}

	/* IEnergyContainerItem */
	@Override
	public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {

		int metadata = ItemHelper.getItemDamage(container);
		if (metadata <= Types.POTATO.ordinal()) {
			return 0;
		}
		if (container.stackTagCompound == null) {
			EnergyHelper.setDefaultEnergyTag(container, 0);
		}
		int stored = container.stackTagCompound.getInteger("Energy");
		int receive = Math.min(maxReceive, Math.min(STORAGE[metadata] - stored, RECEIVE[metadata]));

		if (!simulate && container.getItemDamage() != Types.CREATIVE.ordinal()) {
			stored += receive;
			container.stackTagCompound.setInteger("Energy", stored);
		}
		return receive;
	}

	@Override
	public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {

		if (container.stackTagCompound == null) {
			EnergyHelper.setDefaultEnergyTag(container, 0);
		}
		int stored = container.stackTagCompound.getInteger("Energy");
		int extract = Math.min(maxExtract, Math.min(stored, SEND[ItemHelper.getItemDamage(container)]));

		if (!simulate && container.getItemDamage() != Types.CREATIVE.ordinal()) {
			stored -= extract;
			container.stackTagCompound.setInteger("Energy", stored);

			if (stored == 0 && container.getItemDamage() == Types.POTATO.ordinal()) {
				container.func_150996_a(Items.baked_potato);
			}
		}
		return extract;
	}

	@Override
	public int getEnergyStored(ItemStack container) {

		if (container.stackTagCompound == null) {
			EnergyHelper.setDefaultEnergyTag(container, 0);
		}
		return container.stackTagCompound.getInteger("Energy");
	}

	@Override
	public int getMaxEnergyStored(ItemStack container) {

		return STORAGE[container.getItemDamage()];
	}

	/* HELPERS */
	public boolean isActive(ItemStack stack) {

		return stack.stackTagCompound == null ? false : stack.stackTagCompound.getBoolean("Active");
	}

	public boolean setActiveState(ItemStack stack, boolean state) {

		if (getEnergyStored(stack) > 0) {
			stack.stackTagCompound.setBoolean("Active", state);
			return true;
		}
		stack.stackTagCompound.setBoolean("Active", false);
		return false;
	}

	public static enum Types {
		CREATIVE, POTATO, BASIC, HARDENED, REINFORCED, RESONANT
	}

	public static final String[] NAMES = { "creative", "potato", "basic", "hardened", "reinforced", "resonant" };

	public static final int[] SEND = { 20000, 80, 80, 400, 2000, 10000 };
	public static final int[] RECEIVE = { 20000, 0, 80, 400, 2000, 10000 };
	public static final int[] STORAGE = { 20000, 16000, 80000, 400000, 2000000, 10000000 };

}
