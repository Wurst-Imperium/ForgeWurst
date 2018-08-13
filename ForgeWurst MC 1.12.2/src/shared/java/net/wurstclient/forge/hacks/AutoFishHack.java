/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.lang.reflect.Method;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WEnchantments;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.PlayerControllerUtils;

public final class AutoFishHack extends Hack
{
	private int timer;
	
	public AutoFishHack()
	{
		super("AutoFish", "Automatically catches fish using your\n"
			+ "best fishing rod. If it finds a better\n"
			+ "rod while fishing, it will automatically\n" + "switch to it.");
		setCategory(Category.OTHER);
	}
	
	@Override
	protected void onEnable()
	{
		timer = 0;
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		EntityPlayerSP player = event.getPlayer();
		InventoryPlayer inventory = player.inventory;
		
		if(timer < 0)
		{
			PlayerControllerUtils.windowClick_PICKUP(-timer);
			timer = 15;
			return;
		}
		
		int bestRodValue =
			getRodValue(inventory.getStackInSlot(inventory.currentItem));
		int bestRodSlot = bestRodValue > -1 ? inventory.currentItem : -1;
		
		// search inventory for better rod
		for(int slot = 0; slot < 36; slot++)
		{
			ItemStack stack = inventory.getStackInSlot(slot);
			int rodValue = getRodValue(stack);
			
			if(rodValue > bestRodValue)
			{
				bestRodValue = rodValue;
				bestRodSlot = slot;
			}
		}
		
		if(bestRodSlot == inventory.currentItem)
		{
			// wait for timer
			if(timer > 0)
			{
				timer--;
				return;
			}
			
			// cast rod
			if(player.fishEntity == null)
				rightClick();
			
			return;
		}
		
		if(bestRodSlot == -1)
		{
			ChatUtils.message("Out of fishing rods.");
			setEnabled(false);
			return;
		}
		
		if(bestRodSlot < 9)
		{
			inventory.currentItem = bestRodSlot;
			return;
		}
		
		// place rod in hotbar
		int firstEmptySlot = inventory.getFirstEmptyStack();
		if(firstEmptySlot != -1)
		{
			if(firstEmptySlot >= 9)
				PlayerControllerUtils
					.windowClick_QUICK_MOVE(36 + inventory.currentItem);
			
			PlayerControllerUtils.windowClick_QUICK_MOVE(bestRodSlot);
			
		}else
		{
			PlayerControllerUtils.windowClick_PICKUP(bestRodSlot);
			PlayerControllerUtils
				.windowClick_PICKUP(36 + inventory.currentItem);
			timer = -bestRodSlot;
		}
	}
	
	@SubscribeEvent
	public void onPacketInput(WPacketInputEvent event)
	{
		// check packet type
		if(!(event.getPacket() instanceof SPacketSoundEffect))
			return;
		
		// check sound type
		if(!SoundEvents.ENTITY_BOBBER_SPLASH
			.equals(((SPacketSoundEffect)event.getPacket()).getSound()))
			return;
		
		// catch fish
		rightClick();
	}
	
	private int getRodValue(ItemStack stack)
	{
		if(WItem.isNullOrEmpty(stack)
			|| !(stack.getItem() instanceof ItemFishingRod))
			return -1;
		
		int luckOTSLvl = EnchantmentHelper
			.getEnchantmentLevel(Enchantments.LUCK_OF_THE_SEA, stack);
		int lureLvl =
			EnchantmentHelper.getEnchantmentLevel(Enchantments.LURE, stack);
		int unbreakingLvl = EnchantmentHelper
			.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
		int mendingBonus =
			EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack);
		int noVanishBonus = WEnchantments.hasVanishingCurse(stack) ? 0 : 1;
		
		return luckOTSLvl * 9 + lureLvl * 9 + unbreakingLvl * 2 + mendingBonus
			+ noVanishBonus;
	}
	
	private void rightClick()
	{
		// check held item
		ItemStack stack = WMinecraft.getPlayer().inventory.getCurrentItem();
		if(WItem.isNullOrEmpty(stack)
			|| !(stack.getItem() instanceof ItemFishingRod))
			return;
		
		// right click
		try
		{
			Method rightClickMouse = mc.getClass().getDeclaredMethod(
				wurst.isObfuscated() ? "func_147121_ag" : "rightClickMouse");
			rightClickMouse.setAccessible(true);
			rightClickMouse.invoke(mc);
			
		}catch(ReflectiveOperationException e)
		{
			setEnabled(false);
			throw new RuntimeException(e);
		}
		
		// reset timer
		timer = 15;
	}
}
