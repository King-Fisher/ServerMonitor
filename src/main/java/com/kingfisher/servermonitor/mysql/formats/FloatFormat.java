package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class FloatFormat extends DataFormat {

	public static final FloatFormat FORMAT = new FloatFormat();

	public FloatFormat() {
		super(DataType.FLOAT);
	}
}