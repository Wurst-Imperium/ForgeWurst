/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.compatibility.WPlayer;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.PlayerControllerUtils;
import net.wurstclient.forge.utils.RenderUtils;
import net.wurstclient.forge.utils.RotationUtils;

public final class NukerHack extends Hack
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	private final EnumSetting<Mode> mode =
		new EnumSetting<>("Mode", Mode.values(), Mode.NORMAL);
	
	private final ArrayDeque<Set<BlockPos>> prevBlocks = new ArrayDeque<>();
	private BlockPos currentBlock;
	private float progress;
	private float prevProgress;
	private int id;
	
	public NukerHack()
	{
		super("Nuker", "Automatically breaks blocks around you.");
		setCategory(Category.BLOCKS);
		addSetting(range);
		addSetting(mode);
	}
	
	@Override
	public String getRenderName()
	{
		return mode.getSelected().getRenderName(this);
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
		
		if(currentBlock != null)
			try
			{
				PlayerControllerUtils.setIsHittingBlock(true);
				mc.playerController.resetBlockRemoving();
				currentBlock = null;
				
			}catch(ReflectiveOperationException e)
			{
				throw new RuntimeException(e);
			}
		
		prevBlocks.clear();
		id = 0;
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		EntityPlayerSP player = event.getPlayer();
		
		currentBlock = null;
		Vec3d eyesPos = RotationUtils.getEyesPos().subtract(0.5, 0.5, 0.5);
		BlockPos eyesBlock = new BlockPos(RotationUtils.getEyesPos());
		double rangeSq = Math.pow(range.getValue(), 2);
		int blockRange = (int)Math.ceil(range.getValue());
		
		Stream<BlockPos> stream = StreamSupport.stream(BlockPos
			.getAllInBox(eyesBlock.add(blockRange, blockRange, blockRange),
				eyesBlock.add(-blockRange, -blockRange, -blockRange))
			.spliterator(), true);
		
		List<BlockPos> blocks = stream
			.filter(pos -> eyesPos.squareDistanceTo(new Vec3d(pos)) <= rangeSq)
			.filter(pos -> BlockUtils.canBeClicked(pos))
			.filter(mode.getSelected().getValidator(this))
			.sorted(Comparator.comparingDouble(
				pos -> eyesPos.squareDistanceTo(new Vec3d(pos))))
			.collect(Collectors.toList());
		
		if(player.capabilities.isCreativeMode)
		{
			Stream<BlockPos> stream2 = blocks.parallelStream();
			for(Set<BlockPos> set : prevBlocks)
				stream2 = stream2.filter(pos -> !set.contains(pos));
			List<BlockPos> blocks2 = stream2.collect(Collectors.toList());
			
			prevBlocks.addLast(new HashSet<>(blocks2));
			while(prevBlocks.size() > 5)
				prevBlocks.removeFirst();
			
			if(!blocks2.isEmpty())
				currentBlock = blocks2.get(0);
			
			mc.playerController.resetBlockRemoving();
			progress = 1;
			prevProgress = 1;
			BlockUtils.breakBlocksPacketSpam(blocks2);
			return;
		}
		
		for(BlockPos pos : blocks)
			if(BlockUtils.breakBlockSimple(pos))
			{
				currentBlock = pos;
				break;
			}
		
		if(currentBlock == null)
			mc.playerController.resetBlockRemoving();
		
		if(currentBlock != null && BlockUtils.getHardness(currentBlock) < 1)
			try
			{
				prevProgress = progress;
				progress = PlayerControllerUtils.getCurBlockDamageMP();
				
				if(progress < prevProgress)
					prevProgress = progress;
				
			}catch(ReflectiveOperationException e)
			{
				setEnabled(false);
				throw new RuntimeException(e);
			}
		else
		{
			progress = 1;
			prevProgress = 1;
		}
	}
	
	@SubscribeEvent
	public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		if(!WPlayer.getWorld(player).isRemote)
			return;
		
		if(mode.getSelected() == Mode.ID)
			id = BlockUtils.getId(event.getPos());
	}
	
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		if(currentBlock == null)
			return;
		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		GL11.glPushMatrix();
		GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
			-TileEntityRendererDispatcher.staticPlayerY,
			-TileEntityRendererDispatcher.staticPlayerZ);
		
		AxisAlignedBB box = new AxisAlignedBB(BlockPos.ORIGIN);
		float p =
			prevProgress + (progress - prevProgress) * event.getPartialTicks();
		float red = p * 2F;
		float green = 2 - red;
		
		GL11.glTranslated(currentBlock.getX(), currentBlock.getY(),
			currentBlock.getZ());
		if(p < 1)
		{
			GL11.glTranslated(0.5, 0.5, 0.5);
			GL11.glScaled(p, p, p);
			GL11.glTranslated(-0.5, -0.5, -0.5);
		}
		
		GL11.glColor4f(red, green, 0, 0.25F);
		GL11.glBegin(GL11.GL_QUADS);
		RenderUtils.drawSolidBox(box);
		GL11.glEnd();
		
		GL11.glColor4f(red, green, 0, 0.5F);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils.drawOutlinedBox(box);
		GL11.glEnd();
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private enum Mode
	{
		NORMAL("Normal", n -> n.getName(), (n, p) -> true),
		
		ID("ID", n -> "IDNuker [" + n.id + "]",
			(n, p) -> BlockUtils.getId(p) == n.id),
		
		FLAT("Flat", n -> "FlatNuker",
			(n, p) -> p.getY() >= WMinecraft.getPlayer().getPosition().getY()),
		
		SMASH("Smash", n -> "SmashNuker",
			(n, p) -> BlockUtils.getHardness(p) >= 1);
		
		private final String name;
		private final Function<NukerHack, String> renderName;
		private final BiPredicate<NukerHack, BlockPos> validator;
		
		private Mode(String name, Function<NukerHack, String> renderName,
			BiPredicate<NukerHack, BlockPos> validator)
		{
			this.name = name;
			this.renderName = renderName;
			this.validator = validator;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public String getRenderName(NukerHack n)
		{
			return renderName.apply(n);
		}
		
		public Predicate<BlockPos> getValidator(NukerHack n)
		{
			return p -> validator.test(n, p);
		}
	}
}
