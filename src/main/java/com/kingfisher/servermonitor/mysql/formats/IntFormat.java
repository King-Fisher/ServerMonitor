package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class IntFormat extends DataFormat {

	public static final IntFormat FORMAT = new IntFormat();

	public IntFormat() {
		super(DataType.INT);
	}
}
