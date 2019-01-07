/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class EntityPlayerSPVisitor extends WurstClassVisitor
{
	public EntityPlayerSPVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String onUpdateWalkingPlayer_name;
		String onUpdateWalkingPlayer_desc = "()V";
		String moveEntity_name;
		String moveEntity_desc;
		
		if(mcVersion.isLowerThan("1.11"))
		{
			onUpdateWalkingPlayer_name = obf ? "A" : "onUpdateWalkingPlayer";
			moveEntity_name = obf ? "d" : "moveEntity";
			moveEntity_desc = "(DDD)V";
			
		}else
		{
			String moverType = unmap("net/minecraft/entity/MoverType");
			
			onUpdateWalkingPlayer_name = obf ? "N" : "onUpdateWalkingPlayer";
			moveEntity_name = obf ? "a" : "move";
			moveEntity_desc = "(L" + moverType + ";DDD)V";
		}
		
		registerMethodVisitor(onUpdateWalkingPlayer_name,
			onUpdateWalkingPlayer_desc,
			mv -> new OnUpdateWalkingPlayerVisitor(mv));
		registerMethodVisitor(moveEntity_name, moveEntity_desc,
			mv -> new MoveEntityVisitor(mv));
	}
	
	private static class OnUpdateWalkingPlayerVisitor extends MethodVisitor
	{
		public OnUpdateWalkingPlayerVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
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
		public MoveEntityVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
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
