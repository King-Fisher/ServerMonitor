package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class YearFormat extends DataFormat {

	public static final YearFormat FORMAT = new YearFormat();

	public YearFormat() {
		super(DataType.YEAR);
	}
}