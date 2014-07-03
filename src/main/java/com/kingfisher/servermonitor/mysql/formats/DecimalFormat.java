package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class DecimalFormat extends DataFormat {

	public static final DecimalFormat FORMAT = new DecimalFormat();

	public DecimalFormat(byte precision, byte scale) {
		super(DataType.DECIMAL, precision, scale);
	}

	public DecimalFormat(byte precision) {
		super(DataType.DECIMAL, precision);
	}

	public DecimalFormat() {
		super(DataType.DECIMAL);
	}

	public Byte getPrecision() {
		return (Byte) (_args.length > 0 ? _args[0] : null);
	}

	public byte getScale() {
		return (byte) (_args.length == 2 ? _args[1] : 0);
	}
}