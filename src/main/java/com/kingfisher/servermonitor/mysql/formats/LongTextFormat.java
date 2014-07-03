package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class LongTextFormat extends DataFormat {

	public static final LongTextFormat FORMAT = new LongTextFormat();

	public LongTextFormat() {
		super(DataType.LONGTEXT);
	}
}