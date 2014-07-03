package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class VarCharFormat extends DataFormat {

	public static final VarCharFormat FORMAT = new VarCharFormat();

	public VarCharFormat(short maxSize) {
		super(DataType.VARCHAR, maxSize);
	}

	public VarCharFormat() {
		super(DataType.VARCHAR);
	}

	public short getMaxSize() {
		return (short) (_args.length == 0 ? 1 : _args[0]);
	}
}