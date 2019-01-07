/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.fmlevents;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class WPlayerDamageBlockEvent extends Event
{
	private final BlockPos pos;
	private final EnumFacing facing;
	
	public WPlayerDamageBlockEvent(BlockPos pos, EnumFacing facing)
	{
		this.pos = pos;
		this.facing = facing;
	}
	
	public BlockPos getPos()
	{
		return pos;
	}
	
	public EnumFacing getFacing()
	{
		return facing;
	}
}
