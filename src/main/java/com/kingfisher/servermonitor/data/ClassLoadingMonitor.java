package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import com.kingfisher.servermonitor.mysql.Column;
import com.kingfisher.servermonitor.mysql.RowToAdd;
import com.kingfisher.servermonitor.mysql.Table;
import com.kingfisher.servermonitor.mysql.formats.DateTimeFormat;
import com.kingfisher.servermonitor.mysql.formats.FloatFormat;
import com.kingfisher.servermonitor.mysql.formats.IntFormat;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;

/**
 *
 * @author KingFisher
 */
public final class ClassLoadingMonitor extends DataMonitor implements Runnable {

	private int _i;
	private int _total;
	private int _min;
	private int _max;

	private final Table[] _tables = new Table[]{
		new Table("server_monitor_class_loading", new Column("date", DateTimeFormat.FORMAT), new Column("loaded_mean", FloatFormat.FORMAT), new Column("loaded_min", IntFormat.FORMAT), new Column("loaded_max", IntFormat.FORMAT))
	};

	public ClassLoadingMonitor(ServerMonitor serverMonitor) {
		super(serverMonitor);
	}

	@Override
	public void run() {
		ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
		int loaded = classBean.getLoadedClassCount();
		_i++;
		_total += loaded;
		if (loaded < _min) {
			_min = loaded;
		}
		if (loaded > _max) {
			_max = loaded;
		}
	}

	@Override
	public void initializeData() {
		_i = 0;
		_total = 0;
		_min = Integer.MAX_VALUE;
		_max = Integer.MIN_VALUE;
	}

	@Override
	public void storeData(String date) throws SQLException {
		RowToAdd row = _tables[0].insert();
		row.setValue("date", date);
		row.setValue("loaded_mean", ((float) _total) / _i);
		row.setValue("loaded_min", _min);
		row.setValue("loaded_max", _max);
		getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
	}

	@Override
	public Table[] getTables() {
		return _tables;
	}
}