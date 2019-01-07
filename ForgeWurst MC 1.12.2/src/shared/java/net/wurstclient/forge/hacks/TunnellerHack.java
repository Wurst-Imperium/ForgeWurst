/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import org.lwjgl.opengl.GL11;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockTorch;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.HackList;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.KeyBindingUtils;
import net.wurstclient.forge.utils.PlayerControllerUtils;
import net.wurstclient.forge.utils.RenderUtils;
import net.wurstclient.forge.utils.RotationUtils;

@Hack.DontSaveState
public final class TunnellerHack extends Hack
{
	private final EnumSetting<TunnelSize> size = new EnumSetting<>(
		"Tunnel size", TunnelSize.values(), TunnelSize.SIZE_3X3);
	
	private final SliderSetting limit = new SliderSetting("Limit",
		"Automatically stops once the tunnel\n"
			+ "has reached the given length.\n\n" + "0 = no limit",
		0, 0, 1000, 1,
		v -> v == 0 ? "disabled" : v == 1 ? "1 block" : (int)v + " blocks");
	
	private final CheckboxSetting torches =
		new CheckboxSetting(
			"Place torches", "Places just enough torches\n"
				+ "to prevent mobs from\n" + "spawning inside the tunnel.",
			false);
	
	private BlockPos start;
	private EnumFacing direction;
	private int length;
	
	private Task[] tasks;
	private int[] displayLists = new int[5];
	
	private BlockPos currentBlock;
	private float progress;
	private float prevProgress;
	
	public TunnellerHack()
	{
		super("Tunneller",
			"Automatically digs a tunnel.\n\n" + ChatFormatting.RED
				+ ChatFormatting.BOLD + "WARNING:" + ChatFormatting.RESET
				+ " Although this bot will try to avoid\n"
				+ "lava and other dangers, there is no guarantee\n"
				+ "that it won't die. Only send it out with gear\n"
				+ "that you don't mind losing.");
		setCategory(Category.BLOCKS);
		addSetting(size);
		addSetting(limit);
		addSetting(torches);
	}
	
	@Override
	public String getRenderName()
	{
		if(limit.getValueI() == 0)
			return getName();
		else
			return getName() + " [" + length + "/" + limit.getValueI() + "]";
	}
	
	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
		
		for(int i = 0; i < displayLists.length; i++)
			displayLists[i] = GL11.glGenLists(1);
		
		EntityPlayerSP player = WMinecraft.getPlayer();
		start = new BlockPos(player);
		direction = player.getHorizontalFacing();
		length = 0;
		
		tasks = new Task[]{new DodgeLiquidTask(), new FillInFloorTask(),
			new PlaceTorchTask(), new DigTunnelTask(), new WalkForwardTask()};
		
		updateCyanList();
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
		
		for(int displayList : displayLists)
			GL11.glDeleteLists(displayList, 1);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		HackList hax = wurst.getHax();
		Hack[] incompatibleHax = {hax.autoToolHack, hax.autoWalkHack,
			hax.blinkHack, hax.flightHack, hax.nukerHack, hax.sneakHack};
		for(Hack hack : incompatibleHax)
			hack.setEnabled(false);
		
		if(hax.freecamHack.isEnabled())
			return;
		
		GameSettings gs = mc.gameSettings;
		KeyBinding[] bindings = {gs.keyBindForward, gs.keyBindBack,
			gs.keyBindLeft, gs.keyBindRight, gs.keyBindJump, gs.keyBindSneak};
		for(KeyBinding binding : bindings)
			KeyBindingUtils.setPressed(binding, false);
		
		for(Task task : tasks)
		{
			if(!task.canRun())
				continue;
			
			task.run();
			break;
		}
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
		
		for(int displayList : displayLists)
			GL11.glCallList(displayList);
		
		if(currentBlock != null)
		{
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
			
			AxisAlignedBB box2 = new AxisAlignedBB(BlockPos.ORIGIN);
			GL11.glColor4f(red, green, 0, 0.25F);
			GL11.glBegin(GL11.GL_QUADS);
			RenderUtils.drawSolidBox(box2);
			GL11.glEnd();
			GL11.glColor4f(red, green, 0, 0.5F);
			GL11.glBegin(GL11.GL_LINES);
			RenderUtils.drawOutlinedBox(box2);
			GL11.glEnd();
		}
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private void updateCyanList()
	{
		GL11.glNewList(displayLists[0], GL11.GL_COMPILE);
		
		GL11.glPushMatrix();
		GL11.glTranslated(start.getX(), start.getY(), start.getZ());
		GL11.glTranslated(0.5, 0.5, 0.5);
		
		GL11.glColor4f(0, 1, 1, 0.5F);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils
			.drawNode(new AxisAlignedBB(-0.25, -0.25, -0.25, 0.25, 0.25, 0.25));
		GL11.glEnd();
		
		RenderUtils.drawArrow(
			new Vec3d(direction.getDirectionVec()).scale(0.25),
			new Vec3d(direction.getDirectionVec())
				.scale(Math.max(0.5, length)));
		
		GL11.glPopMatrix();
		GL11.glEndList();
	}
	
	private BlockPos offset(BlockPos pos, Vec3i vec)
	{
		return pos.offset(direction.rotateYCCW(), vec.getX()).up(vec.getY());
	}
	
	private int getDistance(BlockPos pos1, BlockPos pos2)
	{
		return Math.abs(pos1.getX() - pos2.getX())
			+ Math.abs(pos1.getY() - pos2.getY())
			+ Math.abs(pos1.getZ() - pos2.getZ());
	}
	
	private static abstract class Task
	{
		public abstract boolean canRun();
		
		public abstract void run();
	}
	
	private class DigTunnelTask extends Task
	{
		private int requiredDistance;
		
		@Override
		public boolean canRun()
		{
			BlockPos player = new BlockPos(WMinecraft.getPlayer());
			BlockPos base = start.offset(direction, length);
			int distance = getDistance(player, base);
			
			if(distance <= 1)
				requiredDistance = size.getSelected().maxRange;
			else if(distance > size.getSelected().maxRange)
				requiredDistance = 1;
			
			return distance <= requiredDistance;
		}
		
		@Override
		public void run()
		{
			BlockPos base = start.offset(direction, length);
			BlockPos from = offset(base, size.getSelected().from);
			BlockPos to = offset(base, size.getSelected().to);
			
			ArrayList<BlockPos> blocks = new ArrayList<>();
			BlockPos.getAllInBox(from, to).forEach(blocks::add);
			Collections.reverse(blocks);
			
			GL11.glNewList(displayLists[1], GL11.GL_COMPILE);
			AxisAlignedBB box = new AxisAlignedBB(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);
			GL11.glColor4f(0, 1, 0, 0.5F);
			for(BlockPos pos : blocks)
			{
				GL11.glPushMatrix();
				GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
				GL11.glBegin(GL11.GL_LINES);
				RenderUtils.drawOutlinedBox(box);
				GL11.glEnd();
				GL11.glPopMatrix();
			}
			GL11.glEndList();
			
			currentBlock = null;
			for(BlockPos pos : blocks)
			{
				if(!BlockUtils.canBeClicked(pos))
					continue;
				
				currentBlock = pos;
				break;
			}
			
			if(currentBlock == null)
			{
				mc.playerController.resetBlockRemoving();
				progress = 1;
				prevProgress = 1;
				
				length++;
				if(limit.getValueI() == 0 || length < limit.getValueI())
					updateCyanList();
				else
				{
					ChatUtils.message("Tunnel completed.");
					setEnabled(false);
				}
				
				return;
			}
			
			wurst.getHax().autoToolHack.equipBestTool(currentBlock, false, true,
				false);
			BlockUtils.breakBlockSimple(currentBlock);
			
			if(WMinecraft.getPlayer().capabilities.isCreativeMode
				|| BlockUtils.getHardness(currentBlock) >= 1)
			{
				progress = 1;
				prevProgress = 1;
				return;
			}
			
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
		}
	}
	
	private class WalkForwardTask extends Task
	{
		@Override
		public boolean canRun()
		{
			BlockPos player = new BlockPos(WMinecraft.getPlayer());
			BlockPos base = start.offset(direction, length);
			
			return getDistance(player, base) > 1;
		}
		
		@Override
		public void run()
		{
			BlockPos base = start.offset(direction, length);
			Vec3d vec = new Vec3d(base).addVector(0.5, 0.5, 0.5);
			RotationUtils.faceVectorForWalking(vec);
			
			KeyBindingUtils.setPressed(mc.gameSettings.keyBindForward, true);
		}
	}
	
	private class FillInFloorTask extends Task
	{
		private final ArrayList<BlockPos> blocks = new ArrayList<>();
		
		@Override
		public boolean canRun()
		{
			BlockPos player = new BlockPos(WMinecraft.getPlayer());
			BlockPos from = offsetFloor(player, size.getSelected().from);
			BlockPos to = offsetFloor(player, size.getSelected().to);
			
			blocks.clear();
			for(BlockPos pos : BlockPos.getAllInBox(from, to))
				if(!BlockUtils.getState(pos).isFullBlock())
					blocks.add(pos);
				
			GL11.glNewList(displayLists[2], GL11.GL_COMPILE);
			AxisAlignedBB box = new AxisAlignedBB(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);
			GL11.glColor4f(1, 1, 0, 0.5F);
			for(BlockPos pos : blocks)
			{
				GL11.glPushMatrix();
				GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
				GL11.glBegin(GL11.GL_LINES);
				RenderUtils.drawOutlinedBox(box);
				GL11.glEnd();
				GL11.glPopMatrix();
			}
			GL11.glEndList();
			
			return !blocks.isEmpty();
		}
		
		private BlockPos offsetFloor(BlockPos pos, Vec3i vec)
		{
			return pos.offset(direction.rotateYCCW(), vec.getX()).down();
		}
		
		@Override
		public void run()
		{
			KeyBindingUtils.setPressed(mc.gameSettings.keyBindSneak, true);
			WMinecraft.getPlayer().motionX = 0;
			WMinecraft.getPlayer().motionZ = 0;
			
			Vec3d eyes = RotationUtils.getEyesPos().addVector(-0.5, -0.5, -0.5);
			Comparator<BlockPos> comparator =
				Comparator.<BlockPos> comparingDouble(
					p -> eyes.squareDistanceTo(new Vec3d(p)));
			
			BlockPos pos = blocks.stream().max(comparator).get();
			
			if(!equipSolidBlock(pos))
			{
				ChatUtils.error(
					"Found a hole in the tunnel's floor but don't have any blocks to fill it with.");
				setEnabled(false);
				return;
			}
			
			if(BlockUtils.getMaterial(pos).isReplaceable())
				BlockUtils.placeBlockSimple(pos);
			else
			{
				wurst.getHax().autoToolHack.equipBestTool(pos, false, true,
					false);
				BlockUtils.breakBlockSimple(pos);
			}
		}
		
		private boolean equipSolidBlock(BlockPos pos)
		{
			for(int slot = 0; slot < 9; slot++)
			{
				// filter out non-block items
				ItemStack stack =
					WMinecraft.getPlayer().inventory.getStackInSlot(slot);
				if(WItem.isNullOrEmpty(stack)
					|| !(stack.getItem() instanceof ItemBlock))
					continue;
				
				Block block = Block.getBlockFromItem(stack.getItem());
				
				// filter out non-solid blocks
				if(!block.getDefaultState().isFullBlock())
					continue;
				
				// filter out blocks that would fall
				if(block instanceof BlockFalling && BlockFalling
					.canFallThrough(BlockUtils.getState(pos.down())))
					continue;
				
				WMinecraft.getPlayer().inventory.currentItem = slot;
				return true;
			}
			
			return false;
		}
	}
	
	private class DodgeLiquidTask extends Task
	{
		private final HashSet<BlockPos> liquids = new HashSet<>();
		private int disableTimer = 60;
		
		@Override
		public boolean canRun()
		{
			if(!liquids.isEmpty())
				return true;
			
			BlockPos base = start.offset(direction, length);
			BlockPos from = offset(base, size.getSelected().from);
			BlockPos to = offset(base, size.getSelected().to);
			int maxY = Math.max(from.getY(), to.getY());
			
			for(BlockPos pos : BlockPos.getAllInBox(from, to))
			{
				// check current & previous blocks
				int maxOffset = Math.min(size.getSelected().maxRange, length);
				for(int i = 0; i <= maxOffset; i++)
				{
					BlockPos pos2 = pos.offset(direction.getOpposite(), i);
					
					if(BlockUtils.getBlock(pos2) instanceof BlockLiquid)
						liquids.add(pos2);
				}
				
				if(BlockUtils.getState(pos).isFullBlock())
					continue;
				
				// check next blocks
				BlockPos pos3 = pos.offset(direction);
				if(BlockUtils.getBlock(pos3) instanceof BlockLiquid)
					liquids.add(pos3);
				
				// check ceiling blocks
				if(pos.getY() == maxY)
				{
					BlockPos pos4 = pos.up();
					
					if(BlockUtils.getBlock(pos4) instanceof BlockLiquid)
						liquids.add(pos4);
				}
			}
			
			if(liquids.isEmpty())
				return false;
			
			ChatUtils.error("The tunnel is flooded, cannot continue.");
			
			GL11.glNewList(displayLists[3], GL11.GL_COMPILE);
			AxisAlignedBB box = new AxisAlignedBB(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);
			GL11.glColor4f(1, 0, 0, 0.5F);
			for(BlockPos pos : liquids)
			{
				GL11.glPushMatrix();
				GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
				GL11.glBegin(GL11.GL_LINES);
				RenderUtils.drawOutlinedBox(box);
				GL11.glEnd();
				GL11.glPopMatrix();
			}
			GL11.glEndList();
			return true;
		}
		
		@Override
		public void run()
		{
			BlockPos player = new BlockPos(WMinecraft.getPlayer());
			KeyBinding forward = mc.gameSettings.keyBindForward;
			
			Vec3d diffVec = new Vec3d(player.subtract(start));
			Vec3d dirVec = new Vec3d(direction.getDirectionVec());
			double dotProduct = diffVec.dotProduct(dirVec);
			
			BlockPos pos1 = start.offset(direction, (int)dotProduct);
			if(!player.equals(pos1))
			{
				RotationUtils.faceVectorForWalking(toVec3d(pos1));
				KeyBindingUtils.setPressed(forward, true);
				return;
			}
			
			BlockPos pos2 = start.offset(direction, Math.max(0, length - 10));
			if(!player.equals(pos2))
			{
				RotationUtils.faceVectorForWalking(toVec3d(pos2));
				KeyBindingUtils.setPressed(forward, true);
				WMinecraft.getPlayer().setSprinting(true);
				return;
			}
			
			BlockPos pos3 = start.offset(direction, length + 1);
			RotationUtils.faceVectorForWalking(toVec3d(pos3));
			KeyBindingUtils.setPressed(forward, false);
			WMinecraft.getPlayer().setSprinting(false);
			
			if(disableTimer > 0)
			{
				disableTimer--;
				return;
			}
			
			setEnabled(false);
		}
		
		private Vec3d toVec3d(BlockPos pos)
		{
			return new Vec3d(pos).addVector(0.5, 0.5, 0.5);
		}
	}
	
	private class PlaceTorchTask extends Task
	{
		private BlockPos lastTorch;
		private BlockPos nextTorch = start;
		
		@Override
		public boolean canRun()
		{
			if(!torches.isChecked())
			{
				lastTorch = null;
				nextTorch = new BlockPos(WMinecraft.getPlayer());
				GL11.glNewList(displayLists[4], GL11.GL_COMPILE);
				GL11.glEndList();
				return false;
			}
			
			if(lastTorch != null)
				nextTorch = lastTorch.offset(direction,
					size.getSelected().torchDistance);
			
			GL11.glNewList(displayLists[4], GL11.GL_COMPILE);
			GL11.glColor4f(1, 1, 0, 0.5F);
			Vec3d torchVec = new Vec3d(nextTorch).addVector(0.5, 0, 0.5);
			RenderUtils.drawArrow(torchVec, torchVec.addVector(0, 0.5, 0));
			GL11.glEndList();
			
			BlockPos base = start.offset(direction, length);
			if(getDistance(start, base) <= getDistance(start, nextTorch))
				return false;
			
			return Blocks.TORCH.canPlaceBlockAt(WMinecraft.getWorld(),
				nextTorch);
		}
		
		@Override
		public void run()
		{
			if(!equipTorch())
			{
				ChatUtils.error("Out of torches.");
				setEnabled(false);
				return;
			}
			
			KeyBindingUtils.setPressed(mc.gameSettings.keyBindSneak, true);
			BlockUtils.placeBlockSimple(nextTorch);
			
			if(BlockUtils.getBlock(nextTorch) instanceof BlockTorch)
				lastTorch = nextTorch;
		}
		
		private boolean equipTorch()
		{
			for(int slot = 0; slot < 9; slot++)
			{
				// filter out non-block items
				ItemStack stack =
					WMinecraft.getPlayer().inventory.getStackInSlot(slot);
				if(WItem.isNullOrEmpty(stack)
					|| !(stack.getItem() instanceof ItemBlock))
					continue;
				
				// filter out non-torch blocks
				Block block = Block.getBlockFromItem(stack.getItem());
				if(!(block instanceof BlockTorch))
					continue;
				
				WMinecraft.getPlayer().inventory.currentItem = slot;
				return true;
			}
			
			return false;
		}
	}
	
	private enum TunnelSize
	{
		SIZE_1X2("1x2", new Vec3i(0, 1, 0), new Vec3i(0, 0, 0), 4, 13),
		
		SIZE_3X3("3x3", new Vec3i(1, 2, 0), new Vec3i(-1, 0, 0), 4, 11);
		
		private final String name;
		private final Vec3i from;
		private final Vec3i to;
		private final int maxRange;
		private final int torchDistance;
		
		private TunnelSize(String name, Vec3i from, Vec3i to, int maxRange,
			int torchDistance)
		{
			this.name = name;
			this.from = from;
			this.to = to;
			this.maxRange = maxRange;
			this.torchDistance = torchDistance;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
