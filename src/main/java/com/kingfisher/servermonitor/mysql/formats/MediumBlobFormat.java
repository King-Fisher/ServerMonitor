package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class MediumBlobFormat extends DataFormat {

	public static final MediumBlobFormat FORMAT = new MediumBlobFormat();

	public MediumBlobFormat() {
		super(DataType.MEDIUMBLOB);
	}
}