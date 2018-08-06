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

public final class TileEntityRendererDispatcherVisitor extends ClassVisitor
{
	private String render_name;
	private String render_desc;
	
	public TileEntityRendererDispatcherVisitor(int api, ClassVisitor cv,
		boolean obfuscated)
	{
		super(api, cv);
		
		render_name = obfuscated ? "a" : "renderTileEntity";
		render_desc = obfuscated ? "(Laqk;FI)V"
			: "(Lnet/minecraft/tileentity/TileEntity;FI)V";
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(name.equals(render_name) && desc.equals(render_desc))
			return new RenderVisitor(Opcodes.ASM4, mv);
		else
			return mv;
	}
	
	private static class RenderVisitor extends MethodVisitor
	{
		public RenderVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out.println(
				"TileEntityRendererDispatcherVisitor.RenderVisitor.visitCode()");
			super.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"renderTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z",
				false);
			Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, l1);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
}
