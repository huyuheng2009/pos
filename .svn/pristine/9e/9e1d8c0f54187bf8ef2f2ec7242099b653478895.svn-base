package com.yogapay.core.server.tlv;

import org.jpos.iso.ISOException;

import java.nio.CharBuffer;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author yinheli <me@yinheli.com>
 */
public class GBKTLVList {
	private Vector<GBKTLVMsg> tags = new Vector();
	private String tagToFind = null;
	private int indexLastOccurrence = -1;

	public void unpack(String buf)
			throws ISOException {
		CharBuffer buffer = CharBuffer.wrap(buf);

		while (hasNext(buffer)) {
			GBKTLVMsg currentNode = getTLVMsg(buffer);
			if (currentNode != null)
				append(currentNode);
		}
	}

	public Enumeration elements() {
		return this.tags.elements();
	}

	public void unpack(String buf, int offset)
			throws ISOException {
		CharBuffer buffer = CharBuffer.wrap(buf, offset, buf.length());

		while (hasNext(buffer)) {
			GBKTLVMsg currentNode = getTLVMsg(buffer);
			append(currentNode);
		}
	}

	public void append(GBKTLVMsg tlvToAppend) {
		this.tags.add(tlvToAppend);
	}

	public void append(String tag, String value) {
		append(new GBKTLVMsg(tag, value));
	}

	public void deleteByIndex(int index) {
		this.tags.remove(index);
	}

	public void deleteByTag(String tag) {
		int i = 0;

		while (i < this.tags.size()) {
			GBKTLVMsg tlv2 = (GBKTLVMsg) this.tags.elementAt(i);
			if (tlv2.getTag() == tag)
				this.tags.removeElement(tlv2);
			else
				i++;
		}
	}

	public GBKTLVMsg find(String tag) {
		int i = 0;
		this.tagToFind = tag;
		while (i < this.tags.size()) {
			GBKTLVMsg tlv = (GBKTLVMsg) this.tags.elementAt(i);
			if (tlv.getTag().equals(tag)) {
				this.indexLastOccurrence = i;
				return tlv;
			}
			i++;
		}
		this.indexLastOccurrence = -1;
		return null;
	}

	public int findIndex(String tag) {
		int i = 0;
		this.tagToFind = tag;
		while (i < this.tags.size()) {
			GBKTLVMsg tlv = (GBKTLVMsg) this.tags.elementAt(i);
			if (tlv.getTag().equals(tag)) {
				this.indexLastOccurrence = i;
				return i;
			}
			i++;
		}
		this.indexLastOccurrence = -1;
		return -1;
	}

	public GBKTLVMsg findNextTLV() {
		int i = this.indexLastOccurrence + 1;
		int s = this.tags.size();
		while (i < s) {
			GBKTLVMsg tlv = (GBKTLVMsg) this.tags.elementAt(i);
			if (tlv.getTag().equals(this.tagToFind)) {
				this.indexLastOccurrence = i;
				return tlv;
			}
			i++;
		}
		return null;
	}

	public GBKTLVMsg index(int index) {
		return (GBKTLVMsg) this.tags.get(index);
	}

	public String pack()
			throws ISOException {
		StringBuffer buffer = new StringBuffer();
		for (GBKTLVMsg tlv : this.tags) {
			buffer.append(tlv.getTLV());
		}
		return buffer.toString();
	}

	private GBKTLVMsg getTLVMsg(CharBuffer buffer)
			throws ISOException {
		if (buffer.hasRemaining()) {
			String tag = getTAG(buffer);
			int length = getValueLength(buffer);
			if (length > buffer.remaining())
				throw new ISOException("BAD TLV FORMAT - tag (" + tag + ") length (" + length + ") exceeds available data.");
			String value = null;
			if (length > 0) {
				char[] tmp = new char[length];
				buffer.get(tmp);
				value = new String(tmp);
			}
			return new GBKTLVMsg(tag, value);
		}
		return null;
	}

	private boolean hasNext(CharBuffer buffer) {
		return buffer.hasRemaining();
	}

	private String getTAG(CharBuffer buffer) {
		return new String(new char[]{buffer.get(), buffer.get()});
	}

	private int getValueLength(CharBuffer buffer) {
		String length = new String(new char[]{buffer.get(), buffer.get()});
		return Integer.parseInt(length);
	}

}