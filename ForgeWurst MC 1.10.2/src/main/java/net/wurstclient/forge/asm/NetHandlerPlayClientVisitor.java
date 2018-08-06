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

public final class NetHandlerPlayClientVisitor extends ClassVisitor
{
	private String sendPacket_name;
	private String sendPacket_desc;
	
	public NetHandlerPlayClientVisitor(int api, ClassVisitor cv,
		boolean obfuscated)
	{
		super(api, cv);
		sendPacket_name = obfuscated ? "a" : "sendPacket";
		sendPacket_desc =
			obfuscated ? "(Lfj;)V" : "(Lnet/minecraft/network/Packet;)V";
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(name.equals(sendPacket_name) && desc.equals(sendPacket_desc))
			return new SendPacketVisitor(Opcodes.ASM4, mv);
		else
			return mv;
	}
	
	private static class SendPacketVisitor extends MethodVisitor
	{
		public SendPacketVisitor(int api, MethodVisitor mv)
		{
			super(api, mv);
		}
		
		@Override
		public void visitCode()
		{
			System.out.println(
				"NetHandlerPlayClientVisitor.SendPacketVisitor.visitCode()");
			super.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onSendPacket",
				"(Lnet/minecraft/network/Packet;)Lnet/minecraft/network/Packet;",
				false);
			mv.visitVarInsn(Opcodes.ASTORE, 1);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			Label l2 = new Label();
			mv.visitJumpInsn(Opcodes.IFNONNULL, l2);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(l2);
		}
	}
}
