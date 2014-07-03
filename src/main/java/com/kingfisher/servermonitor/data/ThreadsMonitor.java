package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import com.kingfisher.servermonitor.mysql.Column;
import com.kingfisher.servermonitor.mysql.RowToAdd;
import com.kingfisher.servermonitor.mysql.Table;
import com.kingfisher.servermonitor.mysql.formats.DateTimeFormat;
import com.kingfisher.servermonitor.mysql.formats.FloatFormat;
import com.kingfisher.servermonitor.mysql.formats.IntFormat;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.sql.SQLException;

/**
 *
 * @author KingFisher
 */
public final class ThreadsMonitor extends DataMonitor implements Runnable {

	private int _i;
	private int _total_all;
	private int _min_all;
	private int _max_all;
	private int _total_daemon;
	private int _min_daemon;
	private int _max_daemon;
	private int _total_non_daemon;
	private int _min_non_daemon;
	private int _max_non_daemon;

	private final Table[] _tables = new Table[]{
		new Table("server_monitor_threads", new Column("date", DateTimeFormat.FORMAT), new Column("all_mean", FloatFormat.FORMAT), new Column("all_min", IntFormat.FORMAT), new Column("all_max", IntFormat.FORMAT), new Column("daemon_mean", FloatFormat.FORMAT), new Column("daemon_min", IntFormat.FORMAT), new Column("daemon_max", IntFormat.FORMAT), new Column("non_daemon_mean", FloatFormat.FORMAT), new Column("non_daemon_min", IntFormat.FORMAT), new Column("non_daemon_max", IntFormat.FORMAT))
	};

	public ThreadsMonitor(ServerMonitor serverMonitor) {
		super(serverMonitor);
	}

	@Override
	public void run() {
		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		_i++;
		int all = threadBean.getThreadCount();
		_total_all += all;
		if (all < _min_all) {
			_min_all = all;
		}
		if (all > _max_all) {
			_max_all = all;
		}
		int daemons = threadBean.getDaemonThreadCount();
		_total_daemon += daemons;
		if (daemons < _min_daemon) {
			_min_daemon = daemons;
		}
		if (daemons > _max_daemon) {
			_max_daemon = daemons;
		}
		int nonDaemons = all - daemons;
		_total_non_daemon += nonDaemons;
		if (nonDaemons < _min_non_daemon) {
			_min_non_daemon = nonDaemons;
		}
		if (nonDaemons > _max_non_daemon) {
			_max_non_daemon = nonDaemons;
		}
	}

	@Override
	public void initializeData() {
		_i = 0;
		_total_all = 0;
		_min_all = Integer.MAX_VALUE;
		_max_all = Integer.MIN_VALUE;
		_total_daemon = 0;
		_min_daemon = Integer.MAX_VALUE;
		_max_daemon = Integer.MIN_VALUE;
		_total_non_daemon = 0;
		_min_non_daemon = Integer.MAX_VALUE;
		_max_non_daemon = Integer.MIN_VALUE;
	}

	@Override
	public void storeData(String date) throws SQLException {
		RowToAdd row = _tables[0].insert();
		row.setValue("date", date);
		row.setValue("all_mean", ((float) _total_all) / _i);
		row.setValue("all_min", _min_all);
		row.setValue("all_max", _max_all);
		row.setValue("daemon_mean", ((float) _total_daemon) / _i);
		row.setValue("daemon_min", _min_daemon);
		row.setValue("daemon_max", _max_daemon);
		row.setValue("non_daemon_mean", ((float) _total_non_daemon) / _i);
		row.setValue("non_daemon_min", _min_non_daemon);
		row.setValue("non_daemon_max", _max_non_daemon);
		getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
	}

	@Override
	public Table[] getTables() {
		return _tables;
	}
}