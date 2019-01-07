/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPlayerDamageBlockEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.PlayerControllerUtils;

public final class FastBreakHack extends Hack
{
	public FastBreakHack()
	{
		super("FastBreak", "Allows you to break blocks faster.");
		setCategory(Category.BLOCKS);
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
		try
		{
			PlayerControllerUtils.setBlockHitDelay(0);
			
		}catch(ReflectiveOperationException e)
		{
			setEnabled(false);
			throw new RuntimeException(e);
		}
	}
	
	@SubscribeEvent
	public void onPlayerDamageBlock(WPlayerDamageBlockEvent event)
	{
		try
		{
			float progress = PlayerControllerUtils.getCurBlockDamageMP()
				+ BlockUtils.getHardness(event.getPos());
			
			if(progress >= 1)
				return;
			
		}catch(ReflectiveOperationException e)
		{
			setEnabled(false);
			throw new RuntimeException(e);
		}
		
		WMinecraft.getPlayer().connection.sendPacket(new CPacketPlayerDigging(
			CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(),
			event.getFacing()));
	}
}
