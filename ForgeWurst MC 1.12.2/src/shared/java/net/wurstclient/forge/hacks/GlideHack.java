/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.block.BlockLiquid;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WPlayer;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.BlockUtils;

public final class GlideHack extends Hack
{
	private final SliderSetting fallSpeed = new SliderSetting("Fall speed",
		0.125, 0.005, 0.25, 0.005, ValueDisplay.DECIMAL);
	private final SliderSetting moveSpeed =
		new SliderSetting("Move speed", "Horizontal movement factor.", 1.2, 1,
			5, 0.05, ValueDisplay.PERCENTAGE);
	private final SliderSetting minHeight = new SliderSetting("Min height",
		"Won't glide when you are\n" + "too close to the ground.", 0, 0, 2,
		0.01,
		v -> v == 0 ? "disabled" : ValueDisplay.DECIMAL.getValueString(v));
	
	public GlideHack()
	{
		super("Glide", "Makes you glide down slowly when falling.");
		setCategory(Category.MOVEMENT);
		addSetting(fallSpeed);
		addSetting(moveSpeed);
		addSetting(minHeight);
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
		World world = WPlayer.getWorld(player);
		
		if(!player.isAirBorne || player.isInWater() || player.isInLava()
			|| player.isOnLadder() || player.motionY >= 0)
			return;
		
		if(minHeight.getValue() > 0)
		{
			AxisAlignedBB box = player.getEntityBoundingBox();
			box = box.union(box.offset(0, -minHeight.getValue(), 0));
			// Using expand() with negative values doesn't work in 1.10.2.
			if(world.collidesWithAnyBlock(box))
				return;
			
			BlockPos min =
				new BlockPos(new Vec3d(box.minX, box.minY, box.minZ));
			BlockPos max =
				new BlockPos(new Vec3d(box.maxX, box.maxY, box.maxZ));
			Stream<BlockPos> stream = StreamSupport
				.stream(BlockPos.getAllInBox(min, max).spliterator(), true);
			
			// manual collision check, since liquids don't have bounding boxes
			if(stream.map(BlockUtils::getBlock)
				.anyMatch(b -> b instanceof BlockLiquid))
				return;
		}
		
		player.motionY = Math.max(player.motionY, -fallSpeed.getValue());
		player.jumpMovementFactor *= moveSpeed.getValueF();
	}
}
