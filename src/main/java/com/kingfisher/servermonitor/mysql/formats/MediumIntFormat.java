package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class MediumIntFormat extends DataFormat {

	public static final MediumIntFormat FORMAT = new MediumIntFormat();

	public MediumIntFormat() {
		super(DataType.MEDIUMINT);
	}
}
