package com.yogapay.core.server.jpos;

import java.io.IOException;
import java.net.ServerSocket;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;

public class HEXChannel extends BaseChannel {

	/**
	 * constructor shared by server and client ISOChannels (which have different
	 * signatures)
	 */
	public HEXChannel() {
	}

	/**
	 * constructs a client ISOChannel
	 * 
	 * @param host
	 *            server TCP Address
	 * @param port
	 *            server port number
	 * @param p
	 *            an ISOPackager
	 * @see org.jpos.iso.ISOPackager
	 */
	public HEXChannel(String host, int port, ISOPackager p) {
		super(host, port, p);
	}

	/**
	 * constructs a server ISOChannel
	 * 
	 * @param p
	 *            an ISOPackager
	 * @throws java.io.IOException
	 *             on error
	 * @see org.jpos.iso.ISOPackager
	 */
	public HEXChannel(ISOPackager p) throws IOException {
		super(p);
	}

	/**
	 * constructs a server ISOChannel associated with a Server Socket
	 * 
	 * @param p
	 *            an ISOPackager
	 * @param serverSocket
	 *            where to accept a connection
	 * @throws java.io.IOException
	 *             on error
	 * @see org.jpos.iso.ISOPackager
	 */
	public HEXChannel(ISOPackager p, ServerSocket serverSocket)
			throws IOException {
		super(p, serverSocket);
	}

	@Override
	protected void sendMessageLength(int len) throws IOException {
		serverOut.write(len >> 8);
		serverOut.write(len);
	}

	@Override
	protected int getMessageLength() throws IOException, ISOException {
		byte[] b = new byte[2];
		serverIn.readFully(b, 0, 2);
		return (b[0] & 0xFF) << 8 | b[1] & 0xFF;
	}

	@Override
	public void setHeader(String header) {
		super.setHeader(ISOUtil.hex2byte(header));
	}

	@Override
	protected void unpack(ISOMsg m, byte[] b) throws ISOException {
		super.unpack(m, b);
		// System.out.println("接收："+ISOUtil.hexString(b));
		// System.out.println(ISOUtil.hexdump(b));
	}

	public void send(ISOMsg m) throws IOException, ISOException {
		super.send(m);
		// System.out.println("发送："+ISOUtil.hexString(m.pack()));
		// System.out.println(ISOUtil.hexdump(m.pack()));
	}
}
