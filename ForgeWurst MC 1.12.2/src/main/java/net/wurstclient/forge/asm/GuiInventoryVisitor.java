/*
 * Copyright (C) 2017 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class GuiInventoryVisitor extends ClassVisitor
{
	private String initGui_name;
	private String initGui_desc;
	private String actionPerformed_name;
	private String actionPerformed_desc;
	private String buttonList_name;
	
	public GuiInventoryVisitor(int api, ClassVisitor cv, boolean obfuscated)
	{
		super(api, cv);
		initGui_name = obfuscated ? "b" : "initGui";
		initGui_desc = "()V";
		actionPerformed_name = obfuscated ? "a" : "actionPerformed";
		actionPerformed_desc =
			obfuscated ? "(Lbja;)V" : "(Lnet/minecraft/client/gui/GuiButton;)V";
		buttonList_name = obfuscated ? "field_146292_n" : "buttonList";
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(name.equals(initGui_name) && desc.equals(initGui_desc))
			return new InitGuiVisitor(Opcodes.ASM4, mv);
		else if(name.equals(actionPerformed_name)
			&& desc.equals(actionPerformed_desc))
			return new ActionPerformedVisitor(Opcodes.ASM4, mv);
		else
			return mv;
	}
	
	private class InitGuiVisitor extends MethodVisitor
	{
		public InitGuiVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
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
		public ActionPerformedVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
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
