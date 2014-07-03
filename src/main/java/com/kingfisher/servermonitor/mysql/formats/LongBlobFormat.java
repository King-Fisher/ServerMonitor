package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class LongBlobFormat extends DataFormat {

	public static final LongBlobFormat FORMAT = new LongBlobFormat();

	public LongBlobFormat() {
		super(DataType.LONGBLOB);
	}
}