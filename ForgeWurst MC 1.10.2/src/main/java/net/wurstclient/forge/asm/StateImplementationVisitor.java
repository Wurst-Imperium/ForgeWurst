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

public final class StateImplementationVisitor extends ClassVisitor
{
	private String getAmbientOcclusionLightValue_name;
	private String getAmbientOcclusionLightValue_desc;
	
	private String isNormalCube_name;
	private String isNormalCube_desc;
	
	private String shouldSideBeRendered_name;
	private String shouldSideBeRendered_desc;
	
	public StateImplementationVisitor(int api, ClassVisitor cv,
		boolean obfuscated)
	{
		super(api, cv);
		
		getAmbientOcclusionLightValue_name =
			obfuscated ? "j" : "getAmbientOcclusionLightValue";
		getAmbientOcclusionLightValue_desc = "()F";
		
		isNormalCube_name = obfuscated ? "l" : "isNormalCube";
		isNormalCube_desc = "()Z";
		
		shouldSideBeRendered_name = obfuscated ? "c" : "shouldSideBeRendered";
		shouldSideBeRendered_desc = obfuscated ? "(Laih;Lcm;Lct;)Z"
			: "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z";
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(name.equals(getAmbientOcclusionLightValue_name)
			&& desc.equals(getAmbientOcclusionLightValue_desc))
			return new GetAmbientOcclusionLightValueVisitor(Opcodes.ASM4, mv);
		else if(name.equals(isNormalCube_name)
			&& desc.equals(isNormalCube_desc))
			return new IsNormalCubeVisitor(Opcodes.ASM4, mv);
		else if(name.equals(shouldSideBeRendered_name)
			&& desc.equals(shouldSideBeRendered_desc))
			return new ShouldSideBeRenderedVisitor(Opcodes.ASM4, mv);
		else
			return mv;
	}
	
	private static class GetAmbientOcclusionLightValueVisitor
		extends MethodVisitor
	{
		public GetAmbientOcclusionLightValueVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
		}
		
		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.FRETURN)
			{
				System.out.println(
					"StateImplementationVisitor.GetAmbientOcclusionLightValueVisitor.visitInsn()");
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"getAmbientOcclusionLightValue",
					"(FLnet/minecraft/block/state/IBlockState;)F", false);
			}
			
			super.visitInsn(opcode);
		}
	}
	
	private static class IsNormalCubeVisitor extends MethodVisitor
	{
		public IsNormalCubeVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
		}
		
		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.IRETURN)
			{
				System.out.println(
					"StateImplementationVisitor.IsNormalCubeVisitor.visitInsn()");
				Label l1 = new Label();
				mv.visitJumpInsn(Opcodes.IFEQ, l1);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"isNormalCube",
					"(Lnet/minecraft/block/state/IBlockState;)Z", false);
				mv.visitJumpInsn(Opcodes.IFEQ, l1);
				mv.visitInsn(Opcodes.ICONST_1);
				mv.visitInsn(Opcodes.IRETURN);
				mv.visitLabel(l1);
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitInsn(Opcodes.ICONST_0);
			}
			
			super.visitInsn(opcode);
		}
	}
	
	private static class ShouldSideBeRenderedVisitor extends MethodVisitor
	{
		public ShouldSideBeRenderedVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
		}
		
		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.IRETURN)
			{
				System.out.println(
					"StateImplementationVisitor.ShouldSideBeRenderedVisitor.visitInsn()");
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"shouldSideBeRendered",
					"(ZLnet/minecraft/block/state/IBlockState;)Z", false);
			}
			
			super.visitInsn(opcode);
		}
	}
}
