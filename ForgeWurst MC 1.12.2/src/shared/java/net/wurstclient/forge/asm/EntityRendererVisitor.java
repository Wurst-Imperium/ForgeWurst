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

public final class EntityRendererVisitor extends WurstClassVisitor
{
	private String viewBobbing_name;
	
	public EntityRendererVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String hurtCameraEffect_name = obf ? "d" : "hurtCameraEffect";
		String hurtCameraEffect_desc = "(F)V";
		String setupCameraTransform_name = obf ? "a" : "setupCameraTransform";
		String setupCameraTransform_desc = "(FI)V";
		
		viewBobbing_name =
			obf ? mcVersion.isLowerThan("1.11") ? "e" : "f" : "viewBobbing";
		
		registerMethodVisitor(hurtCameraEffect_name, hurtCameraEffect_desc,
			mv -> new HurtCameraEffectVisitor(mv));
		registerMethodVisitor(setupCameraTransform_name,
			setupCameraTransform_desc,
			mv -> new SetupCameraTransformVisitor(mv));
	}
	
	private static class HurtCameraEffectVisitor extends MethodVisitor
	{
		public HurtCameraEffectVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out.println(
				"EntityRendererVisitor.HurtCameraEffectVisitor.visitCode()");
			
			super.visitCode();
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"hurtCameraEffect", "()Z", false);
			Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, l1);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
	
	private class SetupCameraTransformVisitor extends MethodVisitor
	{
		private boolean foundViewBobbing;
		
		public SetupCameraTransformVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitFieldInsn(int opcode, String owner, String name,
			String desc)
		{
			super.visitFieldInsn(opcode, owner, name, desc);
			
			if(name.equals(viewBobbing_name))
				foundViewBobbing = true;
		}
		
		@Override
		public void visitJumpInsn(int opcode, Label label)
		{
			super.visitJumpInsn(opcode, label);
			
			if(!foundViewBobbing)
				return;
			foundViewBobbing = false;
			
			System.out.println(
				"EntityRendererVisitor.SetupCameraTransformVisitor.visitJumpInsn()");
			
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"cameraTransformViewBobbing", "()Z", false);
			mv.visitJumpInsn(Opcodes.IFEQ, label);
		}
	}
}
