package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class BlobFormat extends DataFormat {

	public static final BlobFormat FORMAT = new BlobFormat();

	public BlobFormat() {
		super(DataType.BLOB);
	}
}