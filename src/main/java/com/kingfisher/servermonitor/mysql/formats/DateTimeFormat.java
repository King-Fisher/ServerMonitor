package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class DateTimeFormat extends DataFormat {

	public static final DateTimeFormat FORMAT = new DateTimeFormat();

	public DateTimeFormat() {
		super(DataType.DATETIME);
	}
}