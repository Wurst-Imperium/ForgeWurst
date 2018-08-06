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

public final class GuiScreenVisitor extends ClassVisitor
{
	private String sendChatMessage_name;
	private String sendChatMessage_desc;
	
	public GuiScreenVisitor(int api, ClassVisitor cv, boolean obfuscated)
	{
		super(api, cv);
		sendChatMessage_name = obfuscated ? "b" : "sendChatMessage";
		sendChatMessage_desc = "(Ljava/lang/String;Z)V";
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(name.equals(sendChatMessage_name)
			&& desc.equals(sendChatMessage_desc))
			return new SendChatMessageVisitor(Opcodes.ASM4, mv);
		else
			return mv;
	}
	
	private static class SendChatMessageVisitor extends MethodVisitor
	{
		public SendChatMessageVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out
				.println("GuiScreenVisitor.SendChatMessageVisitor.visitCode()");
			super.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onClientSendMessage", "(Ljava/lang/String;)Ljava/lang/String;",
				false);
			mv.visitVarInsn(Opcodes.ASTORE, 1);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String",
				"isEmpty", "()Z", false);
			Label l2 = new Label();
			mv.visitJumpInsn(Opcodes.IFEQ, l2);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(l2);
		}
	}
}
