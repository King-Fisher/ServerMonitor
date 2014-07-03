package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class DateFormat extends DataFormat {

	public static final DateFormat FORMAT = new DateFormat();

	public DateFormat() {
		super(DataType.DATE);
	}
}