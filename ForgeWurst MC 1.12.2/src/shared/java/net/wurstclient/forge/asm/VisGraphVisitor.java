/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class VisGraphVisitor extends WurstClassVisitor
{
	public VisGraphVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String blockPos = unmap("net/minecraft/util/math/BlockPos");
		
		String setOpaqueCube_name = obf ? "a" : "setOpaqueCube";
		String setOpaqueCube_desc = "(L" + blockPos + ";)V";
		
		registerMethodVisitor(setOpaqueCube_name, setOpaqueCube_desc,
			mv -> new SetOpaqueCubeVisitor(mv));
	}
	
	private static class SetOpaqueCubeVisitor extends MethodVisitor
	{
		public SetOpaqueCubeVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out
				.println("VisGraphVisitor.SetOpaqueCubeVisitor.visitCode()");
			
			super.visitCode();
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"setOpaqueCube", "()Z", false);
			Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, l1);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
}
