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

public final class EntityRendererVisitor extends ClassVisitor
{
	private String hurtCameraEffect_name;
	private String hurtCameraEffect_desc;
	
	private String setupCameraTransform_name;
	private String setupCameraTransform_desc;
	private String viewBobbingField;
	
	public EntityRendererVisitor(int api, ClassVisitor cv, boolean obfuscated)
	{
		super(api, cv);
		hurtCameraEffect_name = obfuscated ? "d" : "hurtCameraEffect";
		hurtCameraEffect_desc = "(F)V";
		setupCameraTransform_name = obfuscated ? "a" : "setupCameraTransform";
		setupCameraTransform_desc = "(FI)V";
		viewBobbingField = obfuscated ? "f" : "viewBobbing";
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(name.equals(hurtCameraEffect_name)
			&& desc.equals(hurtCameraEffect_desc))
			return new HurtCameraEffectVisitor(Opcodes.ASM4, mv);
		else if(name.equals(setupCameraTransform_name)
			&& desc.equals(setupCameraTransform_desc))
			return new SetupCameraTransformVisitor(Opcodes.ASM4, mv);
		else
			return mv;
	}
	
	private static class HurtCameraEffectVisitor extends MethodVisitor
	{
		public HurtCameraEffectVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
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
		
		public SetupCameraTransformVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
		}
		
		@Override
		public void visitFieldInsn(int opcode, String owner, String name,
			String desc)
		{
			super.visitFieldInsn(opcode, owner, name, desc);
			
			if(name.equals(viewBobbingField))
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
