/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.asm;

import java.util.ArrayList;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.update.Version;

public abstract class WurstClassVisitor extends ClassVisitor
{
	private final ArrayList<MethodVisitorRegistryEntry> methodVisitorRegistry =
		new ArrayList<>();
	protected final Version mcVersion = new Version(WMinecraft.VERSION);
	
	public WurstClassVisitor(ClassVisitor cv)
	{
		super(Opcodes.ASM4, cv);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		for(MethodVisitorRegistryEntry entry : methodVisitorRegistry)
			if(name.equals(entry.name) && desc.equals(entry.desc))
				return entry.factory.createMethodVisitor(mv);
			
		return mv;
	}
	
	protected String unmap(String typeName)
	{
		return FMLDeobfuscatingRemapper.INSTANCE.unmap(typeName);
	}
	
	protected void registerMethodVisitor(String name, String desc,
		MethodVisitorFactory factory)
	{
		methodVisitorRegistry
			.add(new MethodVisitorRegistryEntry(name, desc, factory));
	}
	
	public static interface MethodVisitorFactory
	{
		public MethodVisitor createMethodVisitor(MethodVisitor mv);
	}
	
	private static final class MethodVisitorRegistryEntry
	{
		private final String name;
		private final String desc;
		private final MethodVisitorFactory factory;
		
		public MethodVisitorRegistryEntry(String name, String desc,
			MethodVisitorFactory factory)
		{
			this.name = name;
			this.desc = desc;
			this.factory = factory;
		}
	}
}
