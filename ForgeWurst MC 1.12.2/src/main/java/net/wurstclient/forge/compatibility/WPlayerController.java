/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class WPlayerController
{
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static void processRightClickBlock(BlockPos pos, EnumFacing side,
		Vec3d hitVec)
	{
		mc.playerController.processRightClickBlock(WMinecraft.getPlayer(),
			WMinecraft.getWorld(), pos, side, hitVec, EnumHand.MAIN_HAND);
	}
}
