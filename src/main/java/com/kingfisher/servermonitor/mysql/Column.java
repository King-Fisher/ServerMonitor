package com.kingfisher.servermonitor.mysql;

import com.kingfisher.servermonitor.mysql.formats.DataFormat;
import java.util.Objects;

/**
 *
 * @author KingFisher
 */
public final class Column {

	private final String _name;
	private final DataFormat _format;

	public Column(String name, DataFormat format) {
		_name = name;
		_format = format;
	}

	public String getName() {
		return _name;
	}

	public DataFormat getDataFormat() {
		return _format;
	}

	public String getDefinition() {
		return _name + ' ' + _format.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Column) {
			Column other = (Column) object;
			return other._name.equals(_name) && other._format.equals(_format);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this._name);
		hash = 97 * hash + Objects.hashCode(this._format);
		return hash;
	}
}