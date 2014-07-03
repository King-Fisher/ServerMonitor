package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class CharFormat extends DataFormat {

	public static final CharFormat FORMAT = new CharFormat();

	public CharFormat(short size) {
		super(DataType.CHAR, size);
	}

	public CharFormat() {
		super(DataType.CHAR);
	}

	public short getSize() {
		return (short) (_args.length == 0 ? 1 : _args[0]);
	}
}