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

public final class NetworkManagerVisitor extends WurstClassVisitor
{
	public NetworkManagerVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);
		
		String packet = unmap("net/minecraft/network/Packet");
		
		String channelRead0_name = obf ? "a" : "channelRead0";
		String channelRead0_desc =
			"(Lio/netty/channel/ChannelHandlerContext;L" + packet + ";)V";
		
		registerMethodVisitor(channelRead0_name, channelRead0_desc,
			mv -> new ChannelRead0Visitor(mv));
	}
	
	private static class ChannelRead0Visitor extends MethodVisitor
	{
		private boolean done;
		
		public ChannelRead0Visitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}
		
		@Override
		public void visitJumpInsn(int opcode, Label label)
		{
			super.visitJumpInsn(opcode, label);
			
			if(done || opcode != Opcodes.IFEQ)
				return;
			
			System.out.println(
				"NetworkManagerVisitor.ChannelRead0Visitor.visitJumpInsn()");
			
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onReceivePacket", "(Lnet/minecraft/network/Packet;)Z", false);
			Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, l1);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			
			done = true;
		}
	}
}
