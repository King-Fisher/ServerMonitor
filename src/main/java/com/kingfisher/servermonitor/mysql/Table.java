package com.kingfisher.servermonitor.mysql;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author KingFisher
 */
public final class Table {

	private final String _name;
	private final List<Column> _columns;

	public Table(String name, List<Column> columns) {
		_name = name;
		_columns = Collections.unmodifiableList(columns);
	}

	public Table(String name, Column... columns) {
		this(name, Arrays.asList(columns));
	}

	public String getName() {
		return _name;
	}

	public List<Column> getColumns() {
		return _columns;
	}

	public String getDefinition() {
		StringBuilder builder = new StringBuilder();
		builder.append(_name).append(" (");
		for (Column column : _columns) {
			builder.append(column.getDefinition()).append(", ");
		}
		builder.replace(builder.length() - 2, builder.length(), ")");
		return builder.toString();
	}

	public String create() {
		return "CREATE TABLE " + getDefinition() + ";";
	}

	public String createIfNotExists() {
		return "CREATE TABLE IF NOT EXISTS " + getDefinition() + ";";
	}

	public String delete() {
		return "DROP TABLE " + _name + ";";
	}

	public String deleteIfExists() {
		return "DROP TABLE IF EXISTS " + _name + ";";
	}

	public RowToAdd insert() {
		return new RowToAdd(this);
	}
}