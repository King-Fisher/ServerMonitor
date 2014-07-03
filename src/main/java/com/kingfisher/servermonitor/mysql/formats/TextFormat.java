package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;

/**
 *
 * @author KingFisher
 */
public final class TextFormat extends DataFormat {

	public static final TextFormat FORMAT = new TextFormat();

	public TextFormat() {
		super(DataType.TEXT);
	}
}