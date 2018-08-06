/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class FullbrightHack extends Hack
{
	public FullbrightHack()
	{
		super("Fullbright", "Allows you to see in the dark.");
		setCategory(Category.RENDER);
	}
	
	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		if(isEnabled())
		{
			if(mc.gameSettings.gammaSetting < 16)
				mc.gameSettings.gammaSetting =
					Math.min(mc.gameSettings.gammaSetting + 0.5F, 16);
			
			return;
		}
		
		if(mc.gameSettings.gammaSetting > 0.5F)
			mc.gameSettings.gammaSetting =
				Math.max(mc.gameSettings.gammaSetting - 0.5F, 0.5F);
		else
			MinecraftForge.EVENT_BUS.unregister(this);
	}
}
