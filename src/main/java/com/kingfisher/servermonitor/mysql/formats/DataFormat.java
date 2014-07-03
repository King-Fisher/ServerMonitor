package com.kingfisher.servermonitor.mysql.formats;

import com.kingfisher.servermonitor.mysql.DataType;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author KingFisher
 */
public abstract class DataFormat {

	protected final DataType _type;
	protected final Object[] _args;

	protected DataFormat(DataType type, Object... args) {
		_type = type;
		_args = args;
	}

	public DataType getType() {
		return _type;
	}

	public Object[] getArguments() {
		return _args;
	}

	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(_type.name().replace('_', ' '));
		if (_args.length > 0) {
			builder.append('(');
			for (Object arg : _args) {
				builder.append(arg.toString()).append(',');
			}
			builder.replace(builder.length() - 1, builder.length(), ")");
		}
		return builder.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof DataFormat) {
			DataFormat other = (DataFormat) object;
			if (other._type.equals(_type) && (other._args.length == _args.length)) {
				for (int i = 0 ; i < _args.length ; i++) {
					if (!other._args[i].equals(_args[i])) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + Objects.hashCode(this._type);
		hash = 19 * hash + Arrays.deepHashCode(this._args);
		return hash;
	}
}