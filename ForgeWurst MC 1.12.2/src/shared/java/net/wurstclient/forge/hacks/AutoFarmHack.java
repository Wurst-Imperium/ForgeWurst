/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.PlayerControllerUtils;
import net.wurstclient.forge.utils.RenderUtils;
import net.wurstclient.forge.utils.RotationUtils;

public final class AutoFarmHack extends Hack
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final HashMap<BlockPos, Item> plants = new HashMap<>();
	
	private final ArrayDeque<Set<BlockPos>> prevBlocks = new ArrayDeque<>();
	private BlockPos currentBlock;
	private float progress;
	private float prevProgress;
	
	private int displayList;
	private int box;
	private int node;
	
	public AutoFarmHack()
	{
		super("AutoFarm", "Harvests and re-plants crops automatically.\n"
			+ "Works with wheat, carrots, potatoes, beetroots,\n"
			+ "pumpkins, melons, cacti, sugar canes and\n" + "nether warts.");
		setCategory(Category.BLOCKS);
		addSetting(range);
	}
	
	@Override
	protected void onEnable()
	{
		plants.clear();
		displayList = GL11.glGenLists(1);
		box = GL11.glGenLists(1);
		node = GL11.glGenLists(1);
		
		GL11.glNewList(box, GL11.GL_COMPILE);
		AxisAlignedBB box = new AxisAlignedBB(1 / 16.0, 1 / 16.0, 1 / 16.0,
			15 / 16.0, 15 / 16.0, 15 / 16.0);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils.drawOutlinedBox(box);
		GL11.glEnd();
		GL11.glEndList();
		
		GL11.glNewList(node, GL11.GL_COMPILE);
		AxisAlignedBB node =
			new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils.drawNode(node);
		GL11.glEnd();
		GL11.glEndList();
		
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
		GL11.glDeleteLists(displayList, 1);
		GL11.glDeleteLists(box, 1);
		GL11.glDeleteLists(node, 1);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		currentBlock = null;
		Vec3d eyesVec = RotationUtils.getEyesPos().subtract(0.5, 0.5, 0.5);
		BlockPos eyesBlock = new BlockPos(RotationUtils.getEyesPos());
		double rangeSq = Math.pow(range.getValue(), 2);
		int blockRange = (int)Math.ceil(range.getValue());
		
		List<BlockPos> blocks = getBlockStream(eyesBlock, blockRange)
			.filter(pos -> eyesVec.squareDistanceTo(new Vec3d(pos)) <= rangeSq)
			.filter(pos -> BlockUtils.canBeClicked(pos))
			.collect(Collectors.toList());
		
		registerPlants(blocks);
		
		List<BlockPos> blocksToHarvest = new ArrayList<>();
		List<BlockPos> blocksToReplant = new ArrayList<>();
		
		if(!wurst.getHax().freecamHack.isEnabled())
		{
			blocksToHarvest =
				blocks.parallelStream().filter(this::shouldBeHarvested)
					.sorted(Comparator.comparingDouble(
						pos -> eyesVec.squareDistanceTo(new Vec3d(pos))))
					.collect(Collectors.toList());
			blocksToReplant = getBlockStream(eyesBlock, blockRange)
				.filter(
					pos -> eyesVec.squareDistanceTo(new Vec3d(pos)) <= rangeSq)
				.filter(pos -> BlockUtils.getMaterial(pos).isReplaceable())
				.filter(pos -> plants.containsKey(pos))
				.filter(this::canBeReplanted)
				.sorted(Comparator.comparingDouble(
					pos -> eyesVec.squareDistanceTo(new Vec3d(pos))))
				.collect(Collectors.toList());
		}
		
		while(!blocksToReplant.isEmpty())
		{
			BlockPos pos = blocksToReplant.get(0);
			Item neededItem = plants.get(pos);
			if(tryToReplant(pos, neededItem))
				break;
			
			blocksToReplant.removeIf(p -> plants.get(p) == neededItem);
		}
		
		if(blocksToReplant.isEmpty())
			harvest(blocksToHarvest);
		
		updateDisplayList(blocksToHarvest, blocksToReplant);
	}
	
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
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
		
		GL11.glCallList(displayList);
		
		if(currentBlock != null)
		{
			GL11.glPushMatrix();
			
			AxisAlignedBB box = new AxisAlignedBB(BlockPos.ORIGIN);
			float p = prevProgress
				+ (progress - prevProgress) * event.getPartialTicks();
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
		}
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private Stream<BlockPos> getBlockStream(BlockPos center, int range)
	{
		return StreamSupport
			.stream(BlockPos.getAllInBox(center.add(range, range, range),
				center.add(-range, -range, -range)).spliterator(), true);
	}
	
	private boolean shouldBeHarvested(BlockPos pos)
	{
		Block block = BlockUtils.getBlock(pos);
		IBlockState state = BlockUtils.getState(pos);
		
		if(block instanceof BlockCrops)
			return ((BlockCrops)block).isMaxAge(state);
		else if(block instanceof BlockPumpkin || block instanceof BlockMelon)
			return true;
		else if(block instanceof BlockReed)
			return BlockUtils.getBlock(pos.down()) instanceof BlockReed
				&& !(BlockUtils.getBlock(pos.down(2)) instanceof BlockReed);
		else if(block instanceof BlockCactus)
			return BlockUtils.getBlock(pos.down()) instanceof BlockCactus
				&& !(BlockUtils.getBlock(pos.down(2)) instanceof BlockCactus);
		else if(block instanceof BlockNetherWart)
			return state.getValue(BlockNetherWart.AGE).intValue() >= 3;
		
		return false;
	}
	
	private void registerPlants(List<BlockPos> blocks)
	{
		HashMap<Block, Item> seeds = new HashMap<>();
		seeds.put(Blocks.WHEAT, Items.WHEAT_SEEDS);
		seeds.put(Blocks.CARROTS, Items.CARROT);
		seeds.put(Blocks.POTATOES, Items.POTATO);
		seeds.put(Blocks.BEETROOTS, Items.BEETROOT_SEEDS);
		seeds.put(Blocks.PUMPKIN_STEM, Items.PUMPKIN_SEEDS);
		seeds.put(Blocks.MELON_STEM, Items.MELON_SEEDS);
		seeds.put(Blocks.NETHER_WART, Items.NETHER_WART);
		
		plants.putAll(blocks.parallelStream()
			.filter(pos -> seeds.containsKey(BlockUtils.getBlock(pos)))
			.collect(Collectors.toMap(pos -> pos,
				pos -> seeds.get(BlockUtils.getBlock(pos)))));
	}
	
	private boolean canBeReplanted(BlockPos pos)
	{
		Item item = plants.get(pos);
		
		if(item == Items.WHEAT_SEEDS || item == Items.CARROT
			|| item == Items.POTATO || item == Items.BEETROOT_SEEDS
			|| item == Items.PUMPKIN_SEEDS || item == Items.MELON_SEEDS)
			return BlockUtils.getBlock(pos.down()) instanceof BlockFarmland;
		
		if(item == Items.NETHER_WART)
			return BlockUtils.getBlock(pos.down()) instanceof BlockSoulSand;
		
		return false;
	}
	
	private boolean tryToReplant(BlockPos pos, Item neededItem)
	{
		EntityPlayerSP player = WMinecraft.getPlayer();
		ItemStack heldItem = player.getHeldItemMainhand();
		
		if(!WItem.isNullOrEmpty(heldItem) && heldItem.getItem() == neededItem)
		{
			BlockUtils.placeBlockSimple(pos);
			return true;
		}
		
		for(int slot = 0; slot < 36; slot++)
		{
			if(slot == player.inventory.currentItem)
				continue;
			
			ItemStack stack = player.inventory.getStackInSlot(slot);
			if(WItem.isNullOrEmpty(stack) || stack.getItem() != neededItem)
				continue;
			
			if(slot < 9)
				player.inventory.currentItem = slot;
			else if(player.inventory.getFirstEmptyStack() < 9)
				PlayerControllerUtils.windowClick_QUICK_MOVE(slot);
			else if(player.inventory.getFirstEmptyStack() != -1)
			{
				PlayerControllerUtils
					.windowClick_QUICK_MOVE(player.inventory.currentItem + 36);
				PlayerControllerUtils.windowClick_QUICK_MOVE(slot);
			}else
			{
				PlayerControllerUtils
					.windowClick_PICKUP(player.inventory.currentItem + 36);
				PlayerControllerUtils.windowClick_PICKUP(slot);
				PlayerControllerUtils
					.windowClick_PICKUP(player.inventory.currentItem + 36);
			}
			
			return true;
		}
		
		return false;
	}
	
	private void harvest(List<BlockPos> blocksToHarvest)
	{
		if(WMinecraft.getPlayer().capabilities.isCreativeMode)
		{
			Stream<BlockPos> stream3 = blocksToHarvest.parallelStream();
			for(Set<BlockPos> set : prevBlocks)
				stream3 = stream3.filter(pos -> !set.contains(pos));
			List<BlockPos> blocksToHarvest2 =
				stream3.collect(Collectors.toList());
			
			prevBlocks.addLast(new HashSet<>(blocksToHarvest2));
			while(prevBlocks.size() > 5)
				prevBlocks.removeFirst();
			
			if(!blocksToHarvest2.isEmpty())
				currentBlock = blocksToHarvest2.get(0);
			
			mc.playerController.resetBlockRemoving();
			progress = 1;
			prevProgress = 1;
			BlockUtils.breakBlocksPacketSpam(blocksToHarvest2);
			return;
		}
		
		for(BlockPos pos : blocksToHarvest)
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
	
	private void updateDisplayList(List<BlockPos> blocksToHarvest,
		List<BlockPos> blocksToReplant)
	{
		GL11.glNewList(displayList, GL11.GL_COMPILE);
		GL11.glColor4f(0, 1, 0, 0.5F);
		for(BlockPos pos : blocksToHarvest)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
			GL11.glCallList(box);
			GL11.glPopMatrix();
		}
		GL11.glColor4f(0, 1, 1, 0.5F);
		for(BlockPos pos : plants.keySet())
		{
			GL11.glPushMatrix();
			GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
			GL11.glCallList(node);
			GL11.glPopMatrix();
		}
		GL11.glColor4f(1, 0, 0, 0.5F);
		for(BlockPos pos : blocksToReplant)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
			GL11.glCallList(box);
			GL11.glPopMatrix();
		}
		GL11.glEndList();
	}
}
