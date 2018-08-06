/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
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

public final class VisGraphVisitor extends ClassVisitor
{
	private String setOpaqueCube_name;
	private String setOpaqueCube_desc;
	
	public VisGraphVisitor(int api, ClassVisitor cv, boolean obfuscated)
	{
		super(api, cv);
		setOpaqueCube_name = obfuscated ? "a" : "setOpaqueCube";
		setOpaqueCube_desc =
			obfuscated ? "(Lcm;)V" : "(Lnet/minecraft/util/math/BlockPos;)V";
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(name.equals(setOpaqueCube_name) && desc.equals(setOpaqueCube_desc))
			return new SetOpaqueCubeVisitor(Opcodes.ASM4, mv);
		else
			return mv;
	}
	
	private static class SetOpaqueCubeVisitor extends MethodVisitor
	{
		public SetOpaqueCubeVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
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
