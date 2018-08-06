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

public final class NetworkManagerVisitor extends ClassVisitor
{
	private String channelRead0_name;
	private String channelRead0_desc;
	
	public NetworkManagerVisitor(int api, ClassVisitor cv, boolean obfuscated)
	{
		super(api, cv);
		channelRead0_name = obfuscated ? "a" : "channelRead0";
		channelRead0_desc = obfuscated
			? "(Lio/netty/channel/ChannelHandlerContext;Lht;)V"
			: "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V";
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions)
	{
		MethodVisitor mv =
			super.visitMethod(access, name, desc, signature, exceptions);
		
		if(name.equals(channelRead0_name) && desc.equals(channelRead0_desc))
			return new ChannelRead0Visitor(Opcodes.ASM4, mv);
		else
			return mv;
	}
	
	private static class ChannelRead0Visitor extends MethodVisitor
	{
		private boolean done;
		
		public ChannelRead0Visitor(int api, MethodVisitor mv)
		{
			super(api, mv);
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
