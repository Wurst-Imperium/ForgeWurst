/*
 * Copyright © 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WMinecraft;

public final class PlayerControllerUtils
{
	private static final ForgeWurst wurst = ForgeWurst.getForgeWurst();
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static ItemStack windowClick_PICKUP(int slot)
	{
		return mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP,
			WMinecraft.getPlayer());
	}
	
	public static ItemStack windowClick_QUICK_MOVE(int slot)
	{
		return mc.playerController.windowClick(0, slot, 0, ClickType.QUICK_MOVE,
			WMinecraft.getPlayer());
	}
	
	public static ItemStack windowClick_THROW(int slot)
	{
		return mc.playerController.windowClick(0, slot, 1, ClickType.THROW,
			WMinecraft.getPlayer());
	}
	
	public static float getCurBlockDamageMP()
		throws ReflectiveOperationException
	{
		Field field = PlayerControllerMP.class.getDeclaredField(
			wurst.isObfuscated() ? "field_78770_f" : "curBlockDamageMP");
		field.setAccessible(true);
		return field.getFloat(mc.playerController);
	}
	
	public static void setBlockHitDelay(int blockHitDelay)
		throws ReflectiveOperationException
	{
		Field field = PlayerControllerMP.class.getDeclaredField(
			wurst.isObfuscated() ? "field_78781_i" : "blockHitDelay");
		field.setAccessible(true);
		field.setInt(mc.playerController, blockHitDelay);
	}
	
	public static void setIsHittingBlock(boolean isHittingBlock)
		throws ReflectiveOperationException
	{
		Field field = PlayerControllerMP.class.getDeclaredField(
			wurst.isObfuscated() ? "field_78778_j" : "isHittingBlock");
		field.setAccessible(true);
		field.setBoolean(mc.playerController, isHittingBlock);
	}
}
