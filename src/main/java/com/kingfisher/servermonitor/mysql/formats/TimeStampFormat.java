package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class TimeStampFormat extends DataFormat {

	public static final TimeStampFormat FORMAT = new TimeStampFormat();

	public TimeStampFormat() {
		super(DataType.TIMESTAMP);
	}
}