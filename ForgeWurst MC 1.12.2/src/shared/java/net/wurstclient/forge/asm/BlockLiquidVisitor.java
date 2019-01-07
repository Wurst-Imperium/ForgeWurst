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

public final class BlockLiquidVisitor extends WurstClassVisitor
{
	private String axisAlignedBB;
	
	private String FULL_BLOCK_AABB_name;
	private String FULL_BLOCK_AABB_desc;
	
	public BlockLiquidVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String iBlockState = unmap("net/minecraft/block/state/IBlockState");
		String iBlockAccess = unmap(mcVersion.isLowerThan("1.11")
			? "net/minecraft/world/World" : "net/minecraft/world/IBlockAccess");
		String blockPos = unmap("net/minecraft/util/math/BlockPos");
		axisAlignedBB = unmap("net/minecraft/util/math/AxisAlignedBB");
		
		String getCollisionBoundingBox_name =
			obf ? "a" : "getCollisionBoundingBox";
		String getCollisionBoundingBox_desc = "(L" + iBlockState + ";L"
			+ iBlockAccess + ";L" + blockPos + ";)L" + axisAlignedBB + ";";
		
		FULL_BLOCK_AABB_name = obf ? "field_185505_j" : "FULL_BLOCK_AABB";
		FULL_BLOCK_AABB_desc = "L" + axisAlignedBB + ";";
		
		registerMethodVisitor(getCollisionBoundingBox_name,
			getCollisionBoundingBox_desc,
			mv -> new GetCollisionBoundingBoxVisitor(mv));
	}
	
	private class GetCollisionBoundingBoxVisitor extends MethodVisitor
	{
		public GetCollisionBoundingBoxVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitFieldInsn(int opcode, String owner, String name,
			String desc)
		{
			System.out.println(
				"BlockLiquidVisitor.GetCollisionBoundingBoxVisitor.visitFieldInsn()");
			
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"shouldLiquidBeSolid", "()Z", false);
			Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IFEQ, l1);
			mv.visitFieldInsn(Opcodes.GETSTATIC,
				"net/minecraft/block/BlockLiquid", FULL_BLOCK_AABB_name,
				FULL_BLOCK_AABB_desc);
			Label l3 = new Label();
			mv.visitJumpInsn(Opcodes.GOTO, l3);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			super.visitFieldInsn(opcode, owner, name, desc);
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[]{axisAlignedBB});
		}
	}
}
