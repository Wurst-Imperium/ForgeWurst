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

public final class GuiNewChatVisitor extends WurstClassVisitor
{
	private String drawnChatLines_name;
	
	public GuiNewChatVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String iTextComponent = unmap("net/minecraft/util/text/ITextComponent");
		
		String printChatMessageWithOptionalDeletion_name =
			obf ? "a" : "printChatMessageWithOptionalDeletion";
		String printChatMessageWithOptionalDeletion_desc =
			"(L" + iTextComponent + ";I)V";
		
		drawnChatLines_name = obf ? "field_146253_i" : "drawnChatLines";
		
		registerMethodVisitor(printChatMessageWithOptionalDeletion_name,
			printChatMessageWithOptionalDeletion_desc,
			mv -> new PrintChatMessageWithOptionalDeletionVisitor(mv));
	}
	
	private class PrintChatMessageWithOptionalDeletionVisitor
		extends MethodVisitor
	{
		public PrintChatMessageWithOptionalDeletionVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out.println(
				"GuiNewChatVisitor.PrintChatMessageWithOptionalDeletionVisitor.visitCode()");
			
			super.visitCode();
			mv.visitTypeInsn(Opcodes.NEW,
				"net/wurstclient/fmlevents/WChatInputEvent");
			mv.visitInsn(Opcodes.DUP);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD,
				"net/minecraft/client/gui/GuiNewChat", drawnChatLines_name,
				"Ljava/util/List;");
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"net/wurstclient/fmlevents/WChatInputEvent", "<init>",
				"(Lnet/minecraft/util/text/ITextComponent;Ljava/util/List;)V",
				false);
			mv.visitVarInsn(Opcodes.ASTORE, 3);
			mv.visitVarInsn(Opcodes.ALOAD, 3);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onClientReceivedMessage",
				"(Lnet/wurstclient/fmlevents/WChatInputEvent;)Z", false);
			Label l3 = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, l3);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_APPEND, 1,
				new Object[]{"net/wurstclient/fmlevents/WChatInputEvent"}, 0,
				null);
			mv.visitVarInsn(Opcodes.ALOAD, 3);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				"net/wurstclient/fmlevents/WChatInputEvent", "getComponent",
				"()Lnet/minecraft/util/text/ITextComponent;", false);
			mv.visitVarInsn(Opcodes.ASTORE, 1);
		}
	}
}
