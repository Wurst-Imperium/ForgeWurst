/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;

public final class WItem
{
	public static boolean isNullOrEmpty(Item item)
	{
		return item == null || item instanceof ItemAir;
	}
	
	public static boolean isNullOrEmpty(ItemStack stack)
	{
		return stack == null || stack.isEmpty();
	}
	
	public static ItemStack getItemStack(EntityItem entityItem)
	{
		return entityItem.getItem();
	}
	
	public static int getStackSize(ItemStack stack)
	{
		return stack.getCount();
	}
	
	public static float getDestroySpeed(ItemStack stack, IBlockState state)
	{
		return isNullOrEmpty(stack) ? 1 : stack.getDestroySpeed(state);
	}
}
