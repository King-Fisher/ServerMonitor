package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class DoubleFormat extends DataFormat {

	public static final DoubleFormat FORMAT = new DoubleFormat();

	public DoubleFormat() {
		super(DataType.DOUBLE);
	}
}