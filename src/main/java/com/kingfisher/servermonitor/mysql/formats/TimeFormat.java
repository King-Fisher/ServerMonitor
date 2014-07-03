package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class TimeFormat extends DataFormat {

	public static final TimeFormat FORMAT = new TimeFormat();

	public TimeFormat() {
		super(DataType.TIME);
	}
}