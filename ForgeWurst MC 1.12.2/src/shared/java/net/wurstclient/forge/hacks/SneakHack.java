/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPostMotionEvent;
import net.wurstclient.fmlevents.WPreMotionEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.utils.KeyBindingUtils;

public final class SneakHack extends Hack
{
	private final EnumSetting<Mode> mode =
		new EnumSetting<>("Mode", Mode.values(), Mode.LEGIT);
	
	public SneakHack()
	{
		super("Sneak", "Makes you sneak automatically.");
		setCategory(Category.MOVEMENT);
		addSetting(mode);
	}
	
	@Override
	public String getRenderName()
	{
		return getName() + " [" + mode.getSelected() + "]";
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
		
		switch(mode.getSelected())
		{
			case LEGIT:
			KeyBindingUtils.resetPressed(mc.gameSettings.keyBindSneak);
			break;
			
			case PACKET:
			EntityPlayerSP player = WMinecraft.getPlayer();
			player.connection.sendPacket(
				new CPacketEntityAction(player, Action.STOP_SNEAKING));
			break;
		}
	}
	
	@SubscribeEvent
	public void onPreMotion(WPreMotionEvent event)
	{
		switch(mode.getSelected())
		{
			case LEGIT:
			KeyBindingUtils.setPressed(mc.gameSettings.keyBindSneak, true);
			break;
			
			case PACKET:
			KeyBindingUtils.resetPressed(mc.gameSettings.keyBindSneak);
			EntityPlayerSP player = event.getPlayer();
			player.connection.sendPacket(
				new CPacketEntityAction(player, Action.START_SNEAKING));
			player.connection.sendPacket(
				new CPacketEntityAction(player, Action.STOP_SNEAKING));
			break;
		}
	}
	
	@SubscribeEvent
	public void onPostMotion(WPostMotionEvent event)
	{
		if(mode.getSelected() != Mode.PACKET)
			return;
		
		EntityPlayerSP player = event.getPlayer();
		player.connection
			.sendPacket(new CPacketEntityAction(player, Action.STOP_SNEAKING));
		player.connection
			.sendPacket(new CPacketEntityAction(player, Action.START_SNEAKING));
	}
	
	private enum Mode
	{
		PACKET("Packet"),
		LEGIT("Legit");
		
		private final String name;
		
		private Mode(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
