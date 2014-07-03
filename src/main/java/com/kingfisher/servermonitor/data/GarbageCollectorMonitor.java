package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import com.kingfisher.servermonitor.mysql.Column;
import com.kingfisher.servermonitor.mysql.RowToAdd;
import com.kingfisher.servermonitor.mysql.Table;
import com.kingfisher.servermonitor.mysql.formats.BigIntFormat;
import com.kingfisher.servermonitor.mysql.formats.DateTimeFormat;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;

/**
 *
 * @author KingFisher
 */
public final class GarbageCollectorMonitor extends DataMonitor {

	private long _last_c = 0L;
	private long _last_t = 0L;

	private final Table[] _tables = new Table[]{
		new Table("server_monitor_garbage_collector", new Column("date", DateTimeFormat.FORMAT), new Column("collected", BigIntFormat.FORMAT), new Column("collection_time", BigIntFormat.FORMAT))
	};

	public GarbageCollectorMonitor(ServerMonitor serverMonitor) {
		super(serverMonitor);
	}

	@Override
	public void initializeData() {
	}

	@Override
	public void storeData(String date) throws SQLException {
		GarbageCollectorMXBean gcBean = ManagementFactory.getGarbageCollectorMXBeans().get(0);
		long c = gcBean.getCollectionCount();
		long t = gcBean.getCollectionTime();
		RowToAdd row = _tables[0].insert();
		row.setValue("date", date);
		row.setValue("collected", c - _last_c);
		row.setValue("collection_time", t - _last_t);
		getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
		_last_c = c;
		_last_t = t;
	}

	@Override
	public Table[] getTables() {
		return _tables;
	}
}