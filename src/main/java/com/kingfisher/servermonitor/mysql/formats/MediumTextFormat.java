package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class MediumTextFormat extends DataFormat {

	public static final MediumTextFormat FORMAT = new MediumTextFormat();

	public MediumTextFormat() {
		super(DataType.MEDIUMTEXT);
	}
}