package thermalexpansion.item;

import cofh.util.ItemHelper;
import cofh.util.StringHelper;
import cofh.util.inventory.InventoryCraftingFalse;
import cofh.util.oredict.OreDictionaryArbiter;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;

import java.util.List;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class SchematicHelper {

	/* MUST BE PASSED AN INVENTORY WITH 9 SLOTS! */
	public static NBTTagCompound getNBTForSchematic(IInventory craftSlots, ItemStack output) {

		NBTTagCompound nbt = new NBTTagCompound();
		for (int i = 0; i < 9 && i < craftSlots.getSizeInventory(); i++) {
			if (craftSlots.getStackInSlot(i) == null) {
				nbt.removeTag("Slot" + i);
				nbt.removeTag("Name" + i);
				nbt.removeTag("Ore" + i);
			} else {
				NBTTagCompound itemTag = new NBTTagCompound();
				craftSlots.getStackInSlot(i).writeToNBT(itemTag);
				nbt.setTag("Slot" + i, itemTag);
				nbt.setString("Name" + i, craftSlots.getStackInSlot(i).getDisplayName());
				String oreName = ItemHelper.getOreName(craftSlots.getStackInSlot(i));

				if (!oreName.equals(OreDictionaryArbiter.UNKNOWN) && !ItemHelper.isBlacklist(output)) {
					nbt.setString("Ore" + i, oreName);
				}
			}
		}
		nbt.setString("Output", output.stackSize + "x " + output.getDisplayName());
		return nbt;
	}

	public static ItemStack getSchematic(NBTTagCompound nbt) {

		ItemStack returnStack = TEItems.diagramSchematic.copy();
		returnStack.stackTagCompound = nbt;
		return returnStack;
	}

	public static String getOutputName(ItemStack stack) {

		if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("Output")) {
			return ": " + stack.stackTagCompound.getString("Output");
		}
		return "";
	}

	public static ItemStack getOutput(ItemStack schematic, World world) {

		InventoryCrafting tempCraft = new InventoryCraftingFalse(3, 3);
		for (int i = 0; i < 9; i++) {
			tempCraft.setInventorySlotContents(i, getSchematicSlot(schematic, i));
		}
		return ItemHelper.findMatchingRecipe(tempCraft, world);
	}

	public static ItemStack getSchematicSlot(ItemStack schematic, int slot) {

		if (schematic == null) {
			return null;
		}
		if (schematic.stackTagCompound != null && schematic.stackTagCompound.hasKey("Slot" + slot)) {
			return ItemStack.loadItemStackFromNBT(schematic.stackTagCompound.getCompoundTag("Slot" + slot));
		}
		return null;
	}

	public static String getSchematicOreSlot(ItemStack schematic, int slot) {

		if (schematic.stackTagCompound != null && schematic.stackTagCompound.hasKey("Ore" + slot)) {
			return schematic.stackTagCompound.getString("Ore" + slot);
		}
		return null;
	}

	public static boolean isSchematic(ItemStack stack) {

		return stack == null ? false : stack.getUnlocalizedName().contentEquals(TEItems.diagramSchematic.getUnlocalizedName())
				&& stack.getItemDamage() == TEItems.SCHEMATIC_ID;
	}

	/**
	 * Add schematic information. Validity not checked.
	 */
	public static void addSchematicInformation(List list, ItemStack schematic) {

		if (schematic.stackTagCompound == null) {
			list.add(StringHelper.getInfoText("info.cofh.blank"));
			return;
		}
		boolean hasOre = false;
		TMap<String, Integer> aMap = new THashMap<String, Integer>();
		String curName;

		for (int i = 0; i < 9; i++) {
			if (schematic.stackTagCompound.hasKey("Name" + i)) {
				if (schematic.stackTagCompound.hasKey("Ore" + i)) {
					hasOre = true;
					if (StringHelper.isShiftKeyDown()) {
						curName = schematic.stackTagCompound.getString("Ore" + i);
						if (aMap.containsKey(curName)) {
							aMap.put(curName, aMap.get(curName) + 1);
						} else {
							aMap.put(curName, 1);
						}
					}
				} else {
					curName = schematic.stackTagCompound.getString("Name" + i);
					if (aMap.containsKey(curName)) {
						aMap.put(curName, aMap.get(curName) + 1);
					} else {
						aMap.put(curName, 1);
					}
				}
			}
		}
		for (Map.Entry<String, Integer> entry : aMap.entrySet()) {
			list.add(StringHelper.LIGHT_GRAY + entry.getValue() + "x " + entry.getKey());
		}
		if (hasOre && StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
			list.add(StringHelper.shiftForInfo());
		}
	}

}
