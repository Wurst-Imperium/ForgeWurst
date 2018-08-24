/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPlayerDamageBlockEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.utils.BlockUtils;

public final class AutoToolHack extends Hack
{
	private final CheckboxSetting useSwords = new CheckboxSetting("Use swords",
		"Uses swords to break\n" + "leaves, cobwebs, etc.", false);
	private final CheckboxSetting useHands =
		new CheckboxSetting(
			"Use hands", "Uses an empty hand or a\n"
				+ "non-damageable item when\n" + "no applicable tool is found.",
			true);
	private final CheckboxSetting repairMode = new CheckboxSetting(
		"Repair mode", "Won't use tools that are about to break.", false);
	
	public AutoToolHack()
	{
		super("AutoTool",
			"Automatically equips the fastest\n"
				+ "applicable tool in your hotbar\n"
				+ "when you try to break a block.");
		setCategory(Category.BLOCKS);
		addSetting(useSwords);
		addSetting(useHands);
		addSetting(repairMode);
	}
	
	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onPlayerDamageBlock(WPlayerDamageBlockEvent event)
	{
		equipBestTool(event.getPos(), useSwords.isChecked(),
			useHands.isChecked(), repairMode.isChecked());
	}
	
	public void equipBestTool(BlockPos pos, boolean useSwords, boolean useHands,
		boolean repairMode)
	{
		EntityPlayer player = WMinecraft.getPlayer();
		if(player.capabilities.isCreativeMode)
			return;
		
		IBlockState state = BlockUtils.getState(pos);
		
		ItemStack heldItem = player.getHeldItemMainhand();
		float bestSpeed = getDestroySpeed(heldItem, state);
		int bestSlot = -1;
		
		int fallbackSlot = -1;
		InventoryPlayer inventory = player.inventory;
		
		for(int slot = 0; slot < 9; slot++)
		{
			if(slot == inventory.currentItem)
				continue;
			
			ItemStack stack = inventory.getStackInSlot(slot);
			
			if(fallbackSlot == -1 && !isDamageable(stack))
				fallbackSlot = slot;
			
			float speed = getDestroySpeed(stack, state);
			if(speed <= bestSpeed)
				continue;
			
			if(!useSwords && stack.getItem() instanceof ItemSword)
				continue;
			
			if(isTooDamaged(stack, repairMode))
				continue;
			
			bestSpeed = speed;
			bestSlot = slot;
		}
		
		boolean useFallback =
			isDamageable(heldItem) && (isTooDamaged(heldItem, repairMode)
				|| useHands && getDestroySpeed(heldItem, state) <= 1);
		
		if(bestSlot != -1)
		{
			inventory.currentItem = bestSlot;
			return;
		}
		
		if(!useFallback)
			return;
		
		if(fallbackSlot != -1)
		{
			inventory.currentItem = fallbackSlot;
			return;
		}
		
		if(isTooDamaged(heldItem, repairMode))
			if(inventory.currentItem == 8)
				inventory.currentItem = 0;
			else
				inventory.currentItem++;
	}
	
	private float getDestroySpeed(ItemStack stack, IBlockState state)
	{
		float speed = WItem.getDestroySpeed(stack, state);
		
		if(speed > 1)
		{
			int efficiency = EnchantmentHelper
				.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
			if(efficiency > 0 && !WItem.isNullOrEmpty(stack))
				speed += efficiency * efficiency + 1;
		}
		
		return speed;
	}
	
	private boolean isDamageable(ItemStack stack)
	{
		return !WItem.isNullOrEmpty(stack) && stack.getItem().isDamageable();
	}
	
	private boolean isTooDamaged(ItemStack stack, boolean repairMode)
	{
		return repairMode && stack.getMaxDamage() - stack.getItemDamage() <= 4;
	}
}
