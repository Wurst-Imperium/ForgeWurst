/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.fmlevents;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class WShouldSideBeRenderedEvent extends Event
{
	private final IBlockState state;
	private boolean rendered;
	private final boolean normallyRendered;
	
	public WShouldSideBeRenderedEvent(IBlockState state, boolean rendered)
	{
		this.state = state;
		this.rendered = rendered;
		normallyRendered = rendered;
	}
	
	public IBlockState getState()
	{
		return state;
	}
	
	public boolean isRendered()
	{
		return rendered;
	}
	
	public void setRendered(boolean rendered)
	{
		this.rendered = rendered;
	}
	
	public boolean isNormallyRendered()
	{
		return normallyRendered;
	}
}
