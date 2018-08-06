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

public final class PlayerControllerMPVisitor extends ClassVisitor
{
	private String onPlayerDamageBlock_name;
	private String onPlayerDamageBlock_desc;
	
	public PlayerControllerMPVisitor(int api, ClassVisitor cv,
		boolean obfuscated)
	{
		super(api, cv);
		onPlayerDamageBlock_name = obfuscated ? "b" : "onPlayerDamageBlock";
		onPlayerDamageBlock_desc = obfuscated ? "(Let;Lfa;)Z"
			: "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z";
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(name.equals(onPlayerDamageBlock_name)
			&& desc.equals(onPlayerDamageBlock_desc))
			return new OnPlayerDamageBlockVisitor(Opcodes.ASM4, mv);
		else
			return mv;
	}
	
	private static class OnPlayerDamageBlockVisitor extends MethodVisitor
	{
		public OnPlayerDamageBlockVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
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
