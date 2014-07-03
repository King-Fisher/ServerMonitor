package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import com.kingfisher.servermonitor.mysql.Column;
import com.kingfisher.servermonitor.mysql.RowToAdd;
import com.kingfisher.servermonitor.mysql.Table;
import com.kingfisher.servermonitor.mysql.formats.DateTimeFormat;
import com.kingfisher.servermonitor.mysql.formats.FloatFormat;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.SQLException;

/**
 *
 * @author KingFisher
 */
public final class OperatingSystemMonitor extends DataMonitor implements Runnable {

	private int _i;
	private double _total;
	private double _min;
	private double _max;

	private final Table[] _tables = new Table[]{
		new Table("server_monitor_operating_system", new Column("date", DateTimeFormat.FORMAT), new Column("load_mean", FloatFormat.FORMAT), new Column("load_min", FloatFormat.FORMAT), new Column("load_max", FloatFormat.FORMAT))
	};

	public OperatingSystemMonitor(ServerMonitor serverMonitor) {
		super(serverMonitor);
	}

	@Override
	public void run() {
		OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
		double load = osBean.getSystemLoadAverage();
		_i++;
		_total += load;
		if (load < _min) {
			_min = load;
		}
		if (load > _max) {
			_max = load;
		}
	}

	@Override
	public void initializeData() {
		_i = 0;
		_total = 0D;
		_min = Double.MAX_VALUE;
		_max = Double.MIN_VALUE;
	}

	@Override
	public void storeData(String date) throws SQLException {
		RowToAdd row = _tables[0].insert();
		row.setValue("date", date);
		row.setValue("load_mean", (float) (_total / _i));
		row.setValue("load_min", (float) _min);
		row.setValue("load_max", (float) _max);
		getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
	}

	@Override
	public Table[] getTables() {
		return _tables;
	}
}