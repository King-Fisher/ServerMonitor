package com.kingfisher.servermonitor.mysql;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author KingFisher
 */
public final class RowToAdd {

	private final Table _table;
	private final Map<String, Object> _values = new HashMap<>();

	protected RowToAdd(Table table) {
		_table = table;
	}

	public void setValue(String columnName, Object value) {
		for (Column column : _table.getColumns()) {
			if (column.getName().equals(columnName)) {
				_values.put(columnName, value);
				return;
			}
		}
		throw new IllegalArgumentException("Unknown column called: " + columnName + ".");
	}

	public void setValue(Column column, Object value) {
		if (_table.getColumns().contains(column)) {
			_values.put(column.getName(), value);
		} else {
			throw new IllegalArgumentException("Unknown column called: " + column.getName() + ".");
		}
	}

	public String getStatement() {
		StringBuilder statement = new StringBuilder();
		StringBuilder varNames = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for (Entry<String, Object> entry : _values.entrySet()) {
			varNames.append(entry.getKey()).append(", ");
			values.append(entry.getValue()).append(", ");
		}
		varNames.replace(varNames.length() - 2, varNames.length(), "");
		values.replace(values.length() - 2, values.length(), "");
		statement.append("INSERT INTO ").append(_table.getName()).append(" (").append(varNames).append(") VALUES (").append(values).append(");");
		return statement.toString();
	}
}