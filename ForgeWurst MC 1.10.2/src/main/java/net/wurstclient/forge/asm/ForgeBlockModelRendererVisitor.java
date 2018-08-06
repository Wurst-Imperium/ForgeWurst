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

public final class ForgeBlockModelRendererVisitor extends ClassVisitor
{
	private String renderModelFlat_name = "renderModelFlat";
	private String renderModelFlat_obfname = "c";
	private String renderModelSmooth_name = "renderModelSmooth";
	private String renderModelSmooth_obfname = "b";
	private String renderModel_desc =
		"(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/VertexBuffer;ZJ)Z";
	private String renderModel_obfdesc = "(Laih;Lbyl;Lars;Lcm;Lbnt;ZJ)Z";
	
	public ForgeBlockModelRendererVisitor(int api, ClassVisitor cv,
		boolean obfuscated)
	{
		super(api, cv);
		
		// WClassTransformer cannot detect whether or not this class is
		// obfuscated because its name does not change
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(desc.equals(renderModel_desc))
		{
			if(name.equals(renderModelFlat_name))
				return new RenderModelFlatVisitor(Opcodes.ASM4, mv);
			else if(name.equals(renderModelSmooth_name))
				return new RenderModelSmoothVisitor(Opcodes.ASM4, mv);
			
		}else if(desc.equals(renderModel_obfdesc))
			if(name.equals(renderModelFlat_obfname))
				return new RenderModelFlatVisitor(Opcodes.ASM4, mv);
			else if(name.equals(renderModelSmooth_obfname))
				return new RenderModelSmoothVisitor(Opcodes.ASM4, mv);
			
		return mv;
	}
	
	private static class RenderModelFlatVisitor extends MethodVisitor
	{
		public RenderModelFlatVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
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
		public RenderModelSmoothVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
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
