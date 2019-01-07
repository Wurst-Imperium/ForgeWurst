/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lwjgl.opengl.GL11;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.block.BlockLiquid;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketMultiBlockChange.BlockUpdateData;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WChunk;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.RotationUtils;

public final class MobSpawnEspHack extends Hack
{
	private final EnumSetting<DrawDistance> drawDistance = new EnumSetting<>(
		"Draw distance", DrawDistance.values(), DrawDistance.D9);
	private final SliderSetting loadingSpeed =
		new SliderSetting("Loading speed", 1, 1, 5, 1, v -> (int)v + "x");
	
	private final HashMap<Chunk, ChunkScanner> scanners = new HashMap<>();
	private ExecutorService pool;
	
	public MobSpawnEspHack()
	{
		super("MobSpawnESP",
			"Highlights areas where mobs can spawn.\n" + ChatFormatting.YELLOW
				+ "yellow" + ChatFormatting.RESET
				+ " - mobs can spawn at night\n" + ChatFormatting.RED + "red"
				+ ChatFormatting.RESET + " - mobs can always spawn");
		setCategory(Category.RENDER);
		addSetting(drawDistance);
		addSetting(loadingSpeed);
	}
	
	@Override
	protected void onEnable()
	{
		pool = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors(),
			new MinPriorityThreadFactory());
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		
		for(ChunkScanner scanner : new ArrayList<>(scanners.values()))
		{
			if(scanner.displayList != 0)
				GL11.glDeleteLists(scanner.displayList, 1);
			
			scanners.remove(scanner.chunk);
		}
		
		pool.shutdownNow();
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		WorldClient world = WMinecraft.getWorld();
		
		BlockPos eyesBlock = new BlockPos(RotationUtils.getEyesPos());
		int chunkX = eyesBlock.getX() >> 4;
		int chunkZ = eyesBlock.getZ() >> 4;
		int chunkRange = drawDistance.getSelected().chunkRange;
		
		ArrayList<Chunk> chunks = new ArrayList<>();
		for(int x = chunkX - chunkRange; x <= chunkX + chunkRange; x++)
			for(int z = chunkZ - chunkRange; z <= chunkZ + chunkRange; z++)
				chunks.add(world.getChunkFromChunkCoords(x, z));
			
		// create & start scanners for new chunks
		for(Chunk chunk : chunks)
		{
			if(scanners.containsKey(chunk))
				continue;
			
			ChunkScanner scanner = new ChunkScanner(chunk);
			scanners.put(chunk, scanner);
			scanner.future = pool.submit(() -> scanner.scan());
		}
		
		// remove old scanners that are out of range
		for(ChunkScanner scanner : new ArrayList<>(scanners.values()))
		{
			if(Math.abs(WChunk.getX(scanner.chunk) - chunkX) <= chunkRange
				&& Math.abs(WChunk.getZ(scanner.chunk) - chunkZ) <= chunkRange)
				continue;
			
			if(scanner.displayList != 0)
				GL11.glDeleteLists(scanner.displayList, 1);
			
			if(scanner.future != null)
				scanner.future.cancel(true);
			
			scanners.remove(scanner.chunk);
		}
		
		// generate display lists
		Comparator<ChunkScanner> c =
			Comparator.comparingInt(s -> Math.abs(WChunk.getX(s.chunk) - chunkX)
				+ Math.abs(WChunk.getZ(s.chunk) - chunkZ));
		List<ChunkScanner> sortedScanners = scanners.values().stream()
			.filter(s -> s.doneScanning).filter(s -> !s.doneCompiling).sorted(c)
			.limit(loadingSpeed.getValueI()).collect(Collectors.toList());
		
		for(ChunkScanner scanner : sortedScanners)
		{
			if(scanner.displayList == 0)
				scanner.displayList = GL11.glGenLists(1);
			
			scanner.compileDisplayList();
		}
	}
	
	@SubscribeEvent
	public void onPacketInput(WPacketInputEvent event)
	{
		EntityPlayerSP player = WMinecraft.getPlayer();
		WorldClient world = WMinecraft.getWorld();
		if(player == null || world == null)
			return;
		
		Packet packet = event.getPacket();
		Chunk chunk;
		
		if(packet instanceof SPacketBlockChange)
		{
			SPacketBlockChange change = (SPacketBlockChange)packet;
			BlockPos pos = change.getBlockPosition();
			chunk = world.getChunkFromBlockCoords(pos);
			
		}else if(packet instanceof SPacketMultiBlockChange)
		{
			SPacketMultiBlockChange change = (SPacketMultiBlockChange)packet;
			BlockUpdateData[] changedBlocks = change.getChangedBlocks();
			if(changedBlocks.length == 0)
				return;
			
			BlockPos pos = changedBlocks[0].getPos();
			chunk = world.getChunkFromBlockCoords(pos);
			
		}else if(packet instanceof SPacketChunkData)
		{
			SPacketChunkData chunkData = (SPacketChunkData)packet;
			chunk = world.getChunkFromChunkCoords(chunkData.getChunkX(),
				chunkData.getChunkZ());
			
		}else
			return;
		
		ArrayList<Chunk> chunks = new ArrayList<>();
		for(int x = WChunk.getX(chunk) - 1; x <= WChunk.getX(chunk) + 1; x++)
			for(int z = WChunk.getZ(chunk) - 1; z <= WChunk.getZ(chunk)
				+ 1; z++)
				chunks.add(world.getChunkFromChunkCoords(x, z));
			
		for(Chunk chunk2 : chunks)
		{
			ChunkScanner scanner = scanners.get(chunk2);
			if(scanner == null)
				return;
			
			scanner.reset();
			scanner.future = pool.submit(() -> scanner.scan());
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
		
		GL11.glPushMatrix();
		GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
			-TileEntityRendererDispatcher.staticPlayerY,
			-TileEntityRendererDispatcher.staticPlayerZ);
		
		for(ChunkScanner scanner : new ArrayList<>(scanners.values()))
		{
			if(scanner.displayList == 0)
				continue;
			
			GL11.glCallList(scanner.displayList);
		}
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private class ChunkScanner
	{
		public Future future;
		private final Chunk chunk;
		private final Set<BlockPos> red = new HashSet<>();
		private final Set<BlockPos> yellow = new HashSet<>();
		private int displayList;
		
		private boolean doneScanning;
		private boolean doneCompiling;
		
		public ChunkScanner(Chunk chunk)
		{
			this.chunk = chunk;
		}
		
		private void scan()
		{
			BlockPos min = new BlockPos(WChunk.getX(chunk) << 4, 0,
				WChunk.getZ(chunk) << 4);
			BlockPos max = new BlockPos((WChunk.getX(chunk) << 4) + 15, 255,
				(WChunk.getZ(chunk) << 4) + 15);
			
			Stream<BlockPos> stream = StreamSupport
				.stream(BlockPos.getAllInBox(min, max).spliterator(), false);
			
			WorldClient world = WMinecraft.getWorld();
			List<BlockPos> blocks = stream.filter(pos -> {
				return !BlockUtils.getMaterial(pos).blocksMovement()
					&& !(BlockUtils.getBlock(pos) instanceof BlockLiquid)
					&& BlockUtils.getState(pos.down()).isSideSolid(world,
						pos.down(), EnumFacing.UP);
			}).collect(Collectors.toList());
			
			if(Thread.interrupted())
				return;
			
			red.addAll(blocks.stream()
				.filter(pos -> world.getLightFor(EnumSkyBlock.BLOCK, pos) < 8)
				.filter(pos -> world.getLightFor(EnumSkyBlock.SKY, pos) < 8)
				.collect(Collectors.toList()));
			
			if(Thread.interrupted())
				return;
			
			yellow.addAll(blocks.stream().filter(pos -> !red.contains(pos))
				.filter(pos -> world.getLightFor(EnumSkyBlock.BLOCK, pos) < 8)
				.collect(Collectors.toList()));
			doneScanning = true;
		}
		
		private void compileDisplayList()
		{
			GL11.glNewList(displayList, GL11.GL_COMPILE);
			
			GL11.glColor4f(1, 0, 0, 0.5F);
			GL11.glBegin(GL11.GL_LINES);
			new ArrayList<>(red).forEach(pos -> {
				GL11.glVertex3d(pos.getX(), pos.getY() + 0.01, pos.getZ());
				GL11.glVertex3d(pos.getX() + 1, pos.getY() + 0.01,
					pos.getZ() + 1);
				GL11.glVertex3d(pos.getX() + 1, pos.getY() + 0.01, pos.getZ());
				GL11.glVertex3d(pos.getX(), pos.getY() + 0.01, pos.getZ() + 1);
			});
			
			GL11.glColor4f(1, 1, 0, 0.5F);
			new ArrayList<>(yellow).forEach(pos -> {
				GL11.glVertex3d(pos.getX(), pos.getY() + 0.01, pos.getZ());
				GL11.glVertex3d(pos.getX() + 1, pos.getY() + 0.01,
					pos.getZ() + 1);
				GL11.glVertex3d(pos.getX() + 1, pos.getY() + 0.01, pos.getZ());
				GL11.glVertex3d(pos.getX(), pos.getY() + 0.01, pos.getZ() + 1);
			});
			GL11.glEnd();
			
			GL11.glEndList();
			doneCompiling = true;
		}
		
		private void reset()
		{
			if(future != null)
				future.cancel(true);
			
			red.clear();
			yellow.clear();
			
			doneScanning = false;
			doneCompiling = false;
		}
	}
	
	private static class MinPriorityThreadFactory implements ThreadFactory
	{
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		
		public MinPriorityThreadFactory()
		{
			SecurityManager s = System.getSecurityManager();
			group = s != null ? s.getThreadGroup()
				: Thread.currentThread().getThreadGroup();
			namePrefix =
				"pool-min-" + poolNumber.getAndIncrement() + "-thread-";
		}
		
		@Override
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(group, r,
				namePrefix + threadNumber.getAndIncrement(), 0);
			if(t.isDaemon())
				t.setDaemon(false);
			if(t.getPriority() != Thread.MIN_PRIORITY)
				t.setPriority(Thread.MIN_PRIORITY);
			return t;
		}
	}
	
	private enum DrawDistance
	{
		D3("3x3 chunks", 1),
		D5("5x5 chunks", 2),
		D7("7x7 chunks", 3),
		D9("9x9 chunks", 4),
		D11("11x11 chunks", 5),
		D13("13x13 chunks", 6),
		D15("15x15 chunks", 7),
		D17("17x17 chunks", 8),
		D19("19x19 chunks", 9),
		D21("21x21 chunks", 10),
		D23("23x23 chunks", 11),
		D25("25x25 chunks", 12);
		
		private final String name;
		private final int chunkRange;
		
		private DrawDistance(String name, int chunkRange)
		{
			this.name = name;
			this.chunkRange = chunkRange;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
