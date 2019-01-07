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

public final class StateImplementationVisitor extends WurstClassVisitor
{
	public StateImplementationVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String iBlockAccess = unmap("net/minecraft/world/IBlockAccess");
		String blockPos = unmap("net/minecraft/util/math/BlockPos");
		String enumFacing = unmap("net/minecraft/util/EnumFacing");
		
		String getAmbientOcclusionLightValue_name =
			obf ? "j" : "getAmbientOcclusionLightValue";
		String getAmbientOcclusionLightValue_desc = "()F";
		
		String isNormalCube_name = obf ? "l" : "isNormalCube";
		String isNormalCube_desc = "()Z";
		
		String shouldSideBeRendered_name = obf ? "c" : "shouldSideBeRendered";
		String shouldSideBeRendered_desc =
			"(L" + iBlockAccess + ";L" + blockPos + ";L" + enumFacing + ";)Z";
		
		registerMethodVisitor(getAmbientOcclusionLightValue_name,
			getAmbientOcclusionLightValue_desc,
			mv -> new GetAmbientOcclusionLightValueVisitor(mv));
		registerMethodVisitor(isNormalCube_name, isNormalCube_desc,
			mv -> new IsNormalCubeVisitor(mv));
		registerMethodVisitor(shouldSideBeRendered_name,
			shouldSideBeRendered_desc,
			mv -> new ShouldSideBeRenderedVisitor(mv));
	}
	
	private static class GetAmbientOcclusionLightValueVisitor
		extends MethodVisitor
	{
		public GetAmbientOcclusionLightValueVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
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
		public IsNormalCubeVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
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
		public ShouldSideBeRenderedVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
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
