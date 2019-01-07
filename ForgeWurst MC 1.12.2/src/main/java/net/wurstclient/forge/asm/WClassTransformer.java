/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.asm;

import java.util.HashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public final class WClassTransformer implements IClassTransformer
{
	private final HashMap<String, Class<? extends WurstClassVisitor>> visitors =
		new HashMap<>();
	
	private final boolean obfuscated = !FMLDeobfuscatingRemapper.INSTANCE
		.unmap("net/minecraft/client/Minecraft")
		.equals("net/minecraft/client/Minecraft");
	
	public WClassTransformer()
	{
		visitors.put("net.minecraft.block.BlockLiquid",
			BlockLiquidVisitor.class);
		visitors.put("net.minecraft.client.entity.EntityPlayerSP",
			EntityPlayerSPVisitor.class);
		visitors.put("net.minecraft.entity.player.EntityPlayer",
			EntityPlayerVisitor.class);
		visitors.put("net.minecraft.client.renderer.EntityRenderer",
			EntityRendererVisitor.class);
		visitors.put(
			"net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer",
			ForgeBlockModelRendererVisitor.class);
		visitors.put("net.minecraft.client.gui.inventory.GuiContainerCreative",
			GuiContainerCreativeVisitor.class);
		visitors.put("net.minecraft.client.gui.inventory.GuiInventory",
			GuiInventoryVisitor.class);
		visitors.put("net.minecraft.client.gui.GuiNewChat",
			GuiNewChatVisitor.class);
		visitors.put("net.minecraft.client.network.NetHandlerPlayClient",
			NetHandlerPlayClientVisitor.class);
		visitors.put("net.minecraft.network.NetworkManager",
			NetworkManagerVisitor.class);
		visitors.put("net.minecraft.client.multiplayer.PlayerControllerMP",
			PlayerControllerMPVisitor.class);
		visitors.put(
			"net.minecraft.block.state.BlockStateContainer$StateImplementation",
			StateImplementationVisitor.class);
		visitors.put(
			"net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher",
			TileEntityRendererDispatcherVisitor.class);
		visitors.put("net.minecraft.client.renderer.chunk.VisGraph",
			VisGraphVisitor.class);
	}
	
	@Override
	public byte[] transform(String name, String transformedName,
		byte[] basicClass)
	{
		if(!visitors.containsKey(transformedName))
			return basicClass;
		
		System.out.println(
			"Transforming " + transformedName + ", obfuscated=" + obfuscated);
		
		try
		{
			ClassReader reader = new ClassReader(basicClass);
			ClassWriter writer = new ClassWriter(
				ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			ClassVisitor visitor = visitors.get(transformedName)
				.getConstructor(ClassVisitor.class, boolean.class)
				.newInstance(writer, obfuscated);
			reader.accept(visitor, 0);
			return writer.toByteArray();
			
		}catch(Exception e)
		{
			e.printStackTrace();
			return basicClass;
		}
	}
}
