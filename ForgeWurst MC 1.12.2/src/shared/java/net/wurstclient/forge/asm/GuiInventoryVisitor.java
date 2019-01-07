/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class GuiInventoryVisitor extends WurstClassVisitor
{
	private String buttonList_name;
	
	public GuiInventoryVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String guiButton = unmap("net/minecraft/client/gui/GuiButton");
		
		String initGui_name = obf ? "b" : "initGui";
		String initGui_desc = "()V";
		String actionPerformed_name = obf ? "a" : "actionPerformed";
		String actionPerformed_desc = "(L" + guiButton + ";)V";
		
		buttonList_name = obf ? "field_146292_n" : "buttonList";
		
		registerMethodVisitor(initGui_name, initGui_desc,
			mv -> new InitGuiVisitor(mv));
		registerMethodVisitor(actionPerformed_name, actionPerformed_desc,
			mv -> new ActionPerformedVisitor(mv));
	}
	
	private class InitGuiVisitor extends MethodVisitor
	{
		public InitGuiVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.RETURN)
			{
				System.out
					.println("GuiInventoryVisitor.InitGuiVisitor.visitInsn()");
				
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitFieldInsn(Opcodes.GETFIELD,
					"net/minecraft/client/gui/inventory/GuiInventory",
					buttonList_name, "Ljava/util/List;");
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"onGuiInventoryInit", "(Ljava/util/List;)V", false);
			}
			
			super.visitInsn(opcode);
		}
	}
	
	private static class ActionPerformedVisitor extends MethodVisitor
	{
		public ActionPerformedVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.RETURN)
			{
				System.out.println(
					"GuiInventoryVisitor.ActionPerformedVisitor.visitInsn()");
				
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"onGuiInventoryButtonPress",
					"(Lnet/minecraft/client/gui/GuiButton;)V", false);
			}
			
			super.visitInsn(opcode);
		}
	}
}
