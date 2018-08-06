/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.EnumSetting;

public final class AutoSwimHack extends Hack
{
	private final EnumSetting<Mode> mode =
		new EnumSetting<>("Mode", Mode.values(), Mode.DOLPHIN);
	
	public AutoSwimHack()
	{
		super("AutoSwim", "Makes you swim automatically.");
		setCategory(Category.MOVEMENT);
		addSetting(mode);
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
	public void onUpdate(WUpdateEvent event)
	{
		EntityPlayerSP player = event.getPlayer();
		
		if(player.isInWater() && !player.isSneaking()
			&& !GameSettings.isKeyDown(mc.gameSettings.keyBindJump))
			player.motionY += mode.getSelected().upwardsMotion;
	}
	
	private enum Mode
	{
		DOLPHIN("Dolphin", 0.04),
		FISH("Fish", 0.02);
		
		private final String name;
		private final double upwardsMotion;
		
		private Mode(String name, double upwardsMotion)
		{
			this.name = name;
			this.upwardsMotion = upwardsMotion;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
