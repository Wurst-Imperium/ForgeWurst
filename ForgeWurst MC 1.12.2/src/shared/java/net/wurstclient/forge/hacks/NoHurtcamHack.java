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
import net.wurstclient.fmlevents.WHurtCameraEffectEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class NoHurtcamHack extends Hack
{
	public NoHurtcamHack()
	{
		super("NoHurtcam", "Disables the shaking effect when you get hurt.");
		setCategory(Category.RENDER);
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
	public void onHurtCameraEffect(WHurtCameraEffectEvent event)
	{
		event.setCanceled(true);
	}
}
