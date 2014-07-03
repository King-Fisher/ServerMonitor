package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class VarBinaryFormat extends DataFormat {

	public static final VarBinaryFormat FORMAT = new VarBinaryFormat();

	public VarBinaryFormat(short maxSize) {
		super(DataType.VARBINARY, maxSize);
	}

	public VarBinaryFormat() {
		super(DataType.VARBINARY);
	}

	public short getMaxSize() {
		return (short) (_args.length == 0 ? 1 : _args[0]);
	}
}