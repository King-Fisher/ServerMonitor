package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class BinaryFormat extends DataFormat {

	public static final BinaryFormat FORMAT = new BinaryFormat();

	public BinaryFormat(short size) {
		super(DataType.BINARY, size);
	}

	public BinaryFormat() {
		super(DataType.BINARY);
	}

	public short getSize() {
		return (short) (_args.length == 0 ? 1 : _args[0]);
	}
}