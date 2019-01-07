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

public final class NetHandlerPlayClientVisitor extends WurstClassVisitor
{
	public NetHandlerPlayClientVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String packet = unmap("net/minecraft/network/Packet");
		
		String sendPacket_name = obf ? "a" : "sendPacket";
		String sendPacket_desc = "(L" + packet + ";)V";
		
		registerMethodVisitor(sendPacket_name, sendPacket_desc,
			mv -> new SendPacketVisitor(mv));
	}
	
	private static class SendPacketVisitor extends MethodVisitor
	{
		public SendPacketVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
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
