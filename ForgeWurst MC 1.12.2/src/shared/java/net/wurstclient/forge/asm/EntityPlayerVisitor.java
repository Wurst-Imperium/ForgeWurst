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

public final class EntityPlayerVisitor extends WurstClassVisitor
{
	public EntityPlayerVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String jump_name =
			obf ? mcVersion.isLowerThan("1.11") ? "cl" : "cu" : "jump";
		String jump_desc = "()V";
		
		registerMethodVisitor(jump_name, jump_desc, mv -> new JumpVisitor(mv));
	}
	
	private static class JumpVisitor extends MethodVisitor
	{
		public JumpVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out.println("EntityPlayerVisitor.JumpVisitor.visitCode()");
			
			super.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"entityPlayerJump",
				"(Lnet/minecraft/entity/player/EntityPlayer;)Z", false);
			Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, l1);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
}
