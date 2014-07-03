package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;
import java.util.Collection;

/**
 *
 * @author KingFisher
 */
public final class EnumFormat extends DataFormat {

	public EnumFormat(Object... permitted) {
		super(DataType.ENUM, toEnum(permitted));
	}

	public EnumFormat(Collection permitted) {
		this(permitted.toArray());
	}

	private static Object[] toEnum(Object... values) {
		Object[] r = new Object[values.length];
		for (int i = 0 ; i < r.length ; i++) {
			r[i] = '\'' + values[i].toString() + '\'';
		}
		return r;
	}
}