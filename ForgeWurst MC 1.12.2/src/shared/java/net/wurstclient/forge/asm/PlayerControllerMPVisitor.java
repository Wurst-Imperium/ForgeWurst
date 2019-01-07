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

public final class PlayerControllerMPVisitor extends WurstClassVisitor
{
	public PlayerControllerMPVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String blockPos = unmap("net/minecraft/util/math/BlockPos");
		String enumFacing = unmap("net/minecraft/util/EnumFacing");
		
		String onPlayerDamageBlock_name = obf ? "b" : "onPlayerDamageBlock";
		String onPlayerDamageBlock_desc =
			"(L" + blockPos + ";L" + enumFacing + ";)Z";
		
		registerMethodVisitor(onPlayerDamageBlock_name,
			onPlayerDamageBlock_desc, mv -> new OnPlayerDamageBlockVisitor(mv));
	}
	
	private static class OnPlayerDamageBlockVisitor extends MethodVisitor
	{
		public OnPlayerDamageBlockVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out.println(
				"PlayerControllerMPVisitor.OnPlayerDamageBlockVisitor.visitCode()");
			
			super.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onPlayerDamageBlock",
				"(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V",
				false);
		}
	}
}
