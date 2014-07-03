package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class TinyIntFormat extends DataFormat {

	public static final TinyIntFormat FORMAT = new TinyIntFormat();

	public TinyIntFormat() {
		super(DataType.TINYINT);
	}
}
