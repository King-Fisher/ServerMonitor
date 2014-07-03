package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class SmallIntFormat extends DataFormat {

	public static final SmallIntFormat FORMAT = new SmallIntFormat();

	public SmallIntFormat() {
		super(DataType.SMALLINT);
	}
}
