package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import com.kingfisher.servermonitor.mysql.Column;
import com.kingfisher.servermonitor.mysql.RowToAdd;
import com.kingfisher.servermonitor.mysql.Table;
import com.kingfisher.servermonitor.mysql.formats.DateTimeFormat;
import com.kingfisher.servermonitor.mysql.formats.FloatFormat;
import java.sql.SQLException;

/**
 *
 * @author KingFisher
 */
public final class TPSMonitor extends DataMonitor implements Runnable {

	private static final long TICK_DURATION = 50L;
	private static final float TICKS_PER_SECOND = (1F / TICK_DURATION) * 1000F;

	private int _i;
	private float _total;
	private float _min;
	private float _max;
	private long _lastTick;

	private final Table[] _tables = new Table[]{
		new Table("server_monitor_tps", new Column("date", DateTimeFormat.FORMAT), new Column("mean", FloatFormat.FORMAT), new Column("min", FloatFormat.FORMAT), new Column("max", FloatFormat.FORMAT))
	};

	TPSMonitor(ServerMonitor serverMonitor) {
		super(serverMonitor);
		_lastTick = System.currentTimeMillis();
	}

	@Override
	public void run() {
		long t = System.currentTimeMillis();
		long diff = t - _lastTick;
		if (diff >= TICK_DURATION) {
			_i++;
			float tps = (TICKS_PER_SECOND / diff) * TICK_DURATION;
			_total += tps;
			if (tps < _min) {
				_min = tps;
			}
			if (tps > _max) {
				_max = tps;
			}
		}
		_lastTick = t;
	}

	@Override
	public void initializeData() {
		_i = 0;
		_total = 0F;
		_min = Float.MAX_VALUE;
		_max = Float.MIN_VALUE;
	}

	@Override
	public void storeData(String date) throws SQLException {
		RowToAdd row = _tables[0].insert();
		row.setValue("date", date);
		row.setValue("mean", _total / _i);
		row.setValue("min", _min);
		row.setValue("max", _max);
		getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
	}

	@Override
	public Table[] getTables() {
		return _tables;
	}
}