/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.function.Predicate;

import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.EnumSetting;

public final class BunnyHopHack extends Hack
{
	private final EnumSetting<JumpIf> jumpIf =
		new EnumSetting<>("Jump if", JumpIf.values(), JumpIf.SPRINTING);
	
	public BunnyHopHack()
	{
		super("BunnyHop", "Makes you jump automatically.");
		setCategory(Category.MOVEMENT);
		addSetting(jumpIf);
	}
	
	@Override
	public String getRenderName()
	{
		return getName() + " [" + jumpIf.getSelected().name + "]";
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
		if(!player.onGround || player.isSneaking()
			|| player.isInsideOfMaterial(Material.WATER))
			return;
		
		if(jumpIf.getSelected().condition.test(player))
			player.jump();
	}
	
	private enum JumpIf
	{
		SPRINTING("Sprinting",
			p -> p.isSprinting()
				&& (p.moveForward != 0 || p.moveStrafing != 0)),
		
		WALKING("Walking", p -> p.moveForward != 0 || p.moveStrafing != 0),
		
		ALWAYS("Always", p -> true);
		
		private final String name;
		private final Predicate<EntityPlayerSP> condition;
		
		private JumpIf(String name, Predicate<EntityPlayerSP> condition)
		{
			this.name = name;
			this.condition = condition;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
