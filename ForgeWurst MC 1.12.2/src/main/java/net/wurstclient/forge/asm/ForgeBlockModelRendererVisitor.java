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

public final class ForgeBlockModelRendererVisitor extends WurstClassVisitor
{
	public ForgeBlockModelRendererVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		// WClassTransformer cannot detect whether or not this class is
		// obfuscated because its name does not change
		
		String renderModelFlat_name = "renderModelFlat";
		String renderModelFlat_obfname = "c";
		String renderModelSmooth_name = "renderModelSmooth";
		String renderModelSmooth_obfname = "b";
		String renderModel_desc = "(Lnet/minecraft/world/IBlockAccess;"
			+ "Lnet/minecraft/client/renderer/block/model/IBakedModel;"
			+ "Lnet/minecraft/block/state/IBlockState;"
			+ "Lnet/minecraft/util/math/BlockPos;"
			+ "Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z";
		String renderModel_obfdesc = "(Lamy;Lcfy;Lawt;Let;Lbuk;ZJ)Z";
		
		registerMethodVisitor(renderModelFlat_name, renderModel_desc,
			mv -> new RenderModelFlatVisitor(mv));
		registerMethodVisitor(renderModelFlat_obfname, renderModel_obfdesc,
			mv -> new RenderModelFlatVisitor(mv));
		
		registerMethodVisitor(renderModelSmooth_name, renderModel_desc,
			mv -> new RenderModelSmoothVisitor(mv));
		registerMethodVisitor(renderModelSmooth_obfname, renderModel_obfdesc,
			mv -> new RenderModelSmoothVisitor(mv));
	}
	
	private static class RenderModelFlatVisitor extends MethodVisitor
	{
		public RenderModelFlatVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out.println(
				"ForgeBlockModelRendererVisitor.RenderModelFlatVisitor.visitCode()");
			
			super.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 3);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"renderBlockModel",
				"(Lnet/minecraft/block/state/IBlockState;)Z", false);
			Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, l1);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitInsn(Opcodes.IRETURN);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
	
	private static class RenderModelSmoothVisitor extends MethodVisitor
	{
		public RenderModelSmoothVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out.println(
				"ForgeBlockModelRendererVisitor.RenderModelSmoothVisitor.visitCode()");
			
			super.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 3);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"renderBlockModel",
				"(Lnet/minecraft/block/state/IBlockState;)Z", false);
			Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, l1);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitInsn(Opcodes.IRETURN);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
}
