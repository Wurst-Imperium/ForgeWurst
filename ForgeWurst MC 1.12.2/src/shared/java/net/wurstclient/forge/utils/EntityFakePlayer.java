/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.wurstclient.forge.compatibility.WMinecraft;

public class EntityFakePlayer extends EntityOtherPlayerMP
{
	public EntityFakePlayer()
	{
		super(WMinecraft.getWorld(), WMinecraft.getPlayer().getGameProfile());
		copyLocationAndAnglesFrom(WMinecraft.getPlayer());
		
		// fix inventory
		inventory.copyInventory(WMinecraft.getPlayer().inventory);
		getDataManager().set(EntityPlayer.PLAYER_MODEL_FLAG, WMinecraft
			.getPlayer().getDataManager().get(EntityPlayer.PLAYER_MODEL_FLAG));
		
		// fix rotation
		rotationYawHead = WMinecraft.getPlayer().rotationYawHead;
		renderYawOffset = WMinecraft.getPlayer().renderYawOffset;
		
		// fix cape movement
		chasingPosX = posX;
		chasingPosY = posY;
		chasingPosZ = posZ;
		
		// spawn
		WMinecraft.getWorld().addEntityToWorld(getEntityId(), this);
	}
	
	public void resetPlayerPosition()
	{
		WMinecraft.getPlayer().setPositionAndRotation(posX, posY, posZ,
			rotationYaw, rotationPitch);
	}
	
	public void despawn()
	{
		WMinecraft.getWorld().removeEntityFromWorld(getEntityId());
	}
}
