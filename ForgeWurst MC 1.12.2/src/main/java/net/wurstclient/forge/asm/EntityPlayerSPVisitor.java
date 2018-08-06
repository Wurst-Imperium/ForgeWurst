/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class EntityPlayerSPVisitor extends ClassVisitor
{
	private String onUpdateWalkingPlayer_name;
	private String onUpdateWalkingPlayer_desc;
	private String moveEntity_name;
	private String moveEntity_desc;
	
	public EntityPlayerSPVisitor(int api, ClassVisitor cv, boolean obfuscated)
	{
		super(api, cv);
		onUpdateWalkingPlayer_name = obfuscated ? "N" : "onUpdateWalkingPlayer";
		onUpdateWalkingPlayer_desc = "()V";
		moveEntity_name = obfuscated ? "a" : "move";
		moveEntity_desc = obfuscated ? "(Lvv;DDD)V"
			: "(Lnet/minecraft/entity/MoverType;DDD)V";
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(name.equals(onUpdateWalkingPlayer_name)
			&& desc.equals(onUpdateWalkingPlayer_desc))
			return new OnUpdateWalkingPlayerVisitor(Opcodes.ASM4, mv);
		else if(name.equals(moveEntity_name) && desc.equals(moveEntity_desc))
			return new MoveEntityVisitor(Opcodes.ASM4, mv);
		else
			return mv;
	}
	
	private static class OnUpdateWalkingPlayerVisitor extends MethodVisitor
	{
		public OnUpdateWalkingPlayerVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out.println(
				"EntityPlayerSPVisitor.OnUpdateWalkingPlayerVisitor.visitCode()");
			super.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onPreMotion",
				"(Lnet/minecraft/client/entity/EntityPlayerSP;)V", false);
		}
		
		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.RETURN)
			{
				System.out.println(
					"EntityPlayerSPVisitor.OnUpdateWalkingPlayerVisitor.visitInsn()");
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"onPostMotion",
					"(Lnet/minecraft/client/entity/EntityPlayerSP;)V", false);
			}
			
			super.visitInsn(opcode);
		}
	}
	
	private static class MoveEntityVisitor extends MethodVisitor
	{
		public MoveEntityVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out
				.println("EntityPlayerSPVisitor.MoveEntityVisitor.visitCode()");
			super.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onPlayerMove",
				"(Lnet/minecraft/client/entity/EntityPlayerSP;)V", false);
		}
	}
}
