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

public final class BlockLiquidVisitor extends ClassVisitor
{
	private String iBlockState;
	private String iBlockAccess;
	private String blockPos;
	private String axisAlignedBB;
	
	private String getCollisionBoundingBox_name;
	private String getCollisionBoundingBox_desc;
	private String FULL_BLOCK_AABB_name;
	private String FULL_BLOCK_AABB_desc;
	
	public BlockLiquidVisitor(int api, ClassVisitor cv, boolean obfuscated)
	{
		super(api, cv);
		iBlockState =
			obfuscated ? "awt" : "net/minecraft/block/state/IBlockState";
		iBlockAccess = obfuscated ? "amy" : "net/minecraft/world/IBlockAccess";
		blockPos = obfuscated ? "et" : "net/minecraft/util/math/BlockPos";
		axisAlignedBB =
			obfuscated ? "bhb" : "net/minecraft/util/math/AxisAlignedBB";
		
		getCollisionBoundingBox_name =
			obfuscated ? "a" : "getCollisionBoundingBox";
		getCollisionBoundingBox_desc = "(L" + iBlockState + ";L" + iBlockAccess
			+ ";L" + blockPos + ";)L" + axisAlignedBB + ";";
		FULL_BLOCK_AABB_name =
			obfuscated ? "field_185505_j" : "FULL_BLOCK_AABB";
		FULL_BLOCK_AABB_desc = "L" + axisAlignedBB + ";";
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(name.equals(getCollisionBoundingBox_name)
			&& desc.equals(getCollisionBoundingBox_desc))
			return new GetCollisionBoundingBoxVisitor(Opcodes.ASM4, mv);
		else
			return mv;
	}
	
	private class GetCollisionBoundingBoxVisitor extends MethodVisitor
	{
		public GetCollisionBoundingBoxVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
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
