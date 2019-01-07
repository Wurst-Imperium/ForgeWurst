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

public final class GuiScreenVisitor extends WurstClassVisitor
{
	public GuiScreenVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String sendChatMessage_name = obf ? "b" : "sendChatMessage";
		String sendChatMessage_desc = "(Ljava/lang/String;Z)V";
		
		registerMethodVisitor(sendChatMessage_name, sendChatMessage_desc,
			mv -> new SendChatMessageVisitor(mv));
	}
	
	private static class SendChatMessageVisitor extends MethodVisitor
	{
		public SendChatMessageVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
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
