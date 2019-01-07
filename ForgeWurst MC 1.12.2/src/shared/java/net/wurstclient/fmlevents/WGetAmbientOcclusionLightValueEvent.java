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

public final class WGetAmbientOcclusionLightValueEvent extends Event
{
	private final IBlockState state;
	private float lightValue;
	private final float defaultLightValue;
	
	public WGetAmbientOcclusionLightValueEvent(IBlockState state,
		float lightValue)
	{
		this.state = state;
		this.lightValue = lightValue;
		defaultLightValue = lightValue;
	}
	
	public IBlockState getState()
	{
		return state;
	}
	
	public float getLightValue()
	{
		return lightValue;
	}
	
	public void setLightValue(float lightValue)
	{
		this.lightValue = lightValue;
	}
	
	public float getDefaultLightValue()
	{
		return defaultLightValue;
	}
}
