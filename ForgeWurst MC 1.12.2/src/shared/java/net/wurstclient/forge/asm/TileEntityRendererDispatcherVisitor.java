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

public final class TileEntityRendererDispatcherVisitor extends WurstClassVisitor
{
	public TileEntityRendererDispatcherVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String tileEntity = unmap("net/minecraft/tileentity/TileEntity");
		
		String render_name = obf ? "a"
			: mcVersion.isLowerThan("1.12") ? "renderTileEntity" : "render";
		String render_desc = "(L" + tileEntity + ";FI)V";
		
		registerMethodVisitor(render_name, render_desc,
			mv -> new RenderVisitor(mv));
	}
	
	private static class RenderVisitor extends MethodVisitor
	{
		public RenderVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
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
