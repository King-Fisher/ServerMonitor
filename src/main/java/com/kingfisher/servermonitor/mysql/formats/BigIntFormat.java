package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class BigIntFormat extends DataFormat {

	public static final BigIntFormat FORMAT = new BigIntFormat();

	public BigIntFormat() {
		super(DataType.BIGINT);
	}
}