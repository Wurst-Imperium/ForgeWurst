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
import net.minecraft.entity.player.InventoryPlayer;
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
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.PlayerControllerUtils;

public final class AutoFishHack extends Hack
{
	private int timer;
	
	public AutoFishHack()
	{
		super("AutoFish", "Automatically catches fish until\n"
			+ "all of your fishing rods are\n" + "used up.");
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
		
		// select fishing rod in hotbar
		int rodInHotbar = searchFishingRod(0, 9);
		if(rodInHotbar != -1)
		{
			if(inventory.currentItem != rodInHotbar)
			{
				inventory.currentItem = rodInHotbar;
				return;
			}
			
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
		
		int rodInInventory = searchFishingRod(9, 36);
		if(rodInInventory == -1)
		{
			ChatUtils.message("Out of fishing rods.");
			setEnabled(false);
			return;
		}
		
		// find empty hotbar slot
		int hotbarSlot = -1;
		for(int i = 0; i < 9; i++)
		{
			// skip non-empty slots
			if(!WItem.isNullOrEmpty(inventory.getStackInSlot(i)))
				continue;
			
			hotbarSlot = i;
			break;
		}
		
		// place rod in hotbar slot
		PlayerControllerUtils.windowClick_PICKUP(rodInInventory);
		if(hotbarSlot != -1)
			PlayerControllerUtils.windowClick_PICKUP(36 + hotbarSlot);
		else
		{
			PlayerControllerUtils
				.windowClick_PICKUP(36 + inventory.currentItem);
			PlayerControllerUtils.windowClick_PICKUP(rodInInventory);
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
	
	private int searchFishingRod(int from, int to)
	{
		InventoryPlayer inventory = WMinecraft.getPlayer().inventory;
		
		for(int i = from; i < to; i++)
		{
			ItemStack stack = inventory.getStackInSlot(i);
			if(WItem.isNullOrEmpty(stack))
				continue;
			
			if(stack.getItem() instanceof ItemFishingRod)
				return i;
		}
		
		return -1;
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
