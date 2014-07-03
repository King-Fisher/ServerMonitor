package com.kingfisher.servermonitor.data;

import com.kingfisher.servermonitor.ServerMonitor;
import com.kingfisher.servermonitor.mysql.Column;
import com.kingfisher.servermonitor.mysql.RowToAdd;
import com.kingfisher.servermonitor.mysql.Table;
import com.kingfisher.servermonitor.mysql.formats.DateTimeFormat;
import com.kingfisher.servermonitor.mysql.formats.FloatFormat;
import com.kingfisher.servermonitor.mysql.formats.IntFormat;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.sql.SQLException;

/**
 *
 * @author KingFisher
 */
public final class MemoryMonitor extends DataMonitor implements Runnable {

	private static final float MB = 1024F * 1024F;

	private int _i;
	private float _totalAll;
	private float _minAll;
	private float _maxAll;
	private float _totalHeap;
	private float _minHeap;
	private float _maxHeap;
	private float _totalNonHeap;
	private float _minNonHeap;
	private float _maxNonHeap;
	private int _totalFinalization;
	private int _minFinalization;
	private int _maxFinalization;

	private final Table[] _tables = new Table[]{
		new Table("server_monitor_memory", new Column("date", DateTimeFormat.FORMAT), new Column("total_mean", FloatFormat.FORMAT), new Column("total_min", FloatFormat.FORMAT), new Column("total_max", FloatFormat.FORMAT), new Column("heap_mean", FloatFormat.FORMAT), new Column("heap_min", FloatFormat.FORMAT), new Column("heap_max", FloatFormat.FORMAT), new Column("non_heap_mean", FloatFormat.FORMAT), new Column("non_heap_min", FloatFormat.FORMAT), new Column("non_heap_max", FloatFormat.FORMAT), new Column("finalization_mean", FloatFormat.FORMAT), new Column("finalization_min", IntFormat.FORMAT), new Column("finalization_max", IntFormat.FORMAT))
	};

	MemoryMonitor(ServerMonitor serverMonitor) {
		super(serverMonitor);
	}

	@Override
	public void run() {
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
		MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
		int fin = memoryBean.getObjectPendingFinalizationCount();
		_i++;
		_totalFinalization += fin;
		if (fin < _minFinalization) {
			_minFinalization = fin;
		}
		if (fin > _maxFinalization) {
			_maxFinalization = fin;
		}
		float all = (heapUsage.getUsed() + nonHeapUsage.getUsed()) / MB;
		_totalAll += all;
		if (all < _minAll) {
			_minAll = all;
		}
		if (all > _maxAll) {
			_maxAll = all;
		}
		float heap = heapUsage.getUsed() / MB;
		_totalHeap += heap;
		if (heap < _minHeap) {
			_minHeap = heap;
		}
		if (heap > _maxHeap) {
			_maxHeap = heap;
		}
		float nonHeap = nonHeapUsage.getUsed() / MB;
		_totalNonHeap += nonHeap;
		if (nonHeap < _minNonHeap) {
			_minNonHeap = nonHeap;
		}
		if (nonHeap > _maxNonHeap) {
			_maxNonHeap = nonHeap;
		}
	}

	@Override
	public void initializeData() {
		_i = 0;
		_totalAll = 0F;
		_minAll = Float.MAX_VALUE;
		_maxAll = Float.MIN_VALUE;
		_totalHeap = 0F;
		_minHeap = Float.MAX_VALUE;
		_maxHeap = Float.MIN_VALUE;
		_totalNonHeap = 0F;
		_minNonHeap = Float.MAX_VALUE;
		_maxNonHeap = Float.MIN_VALUE;
		_totalFinalization = 0;
		_minFinalization = Integer.MAX_VALUE;
		_maxFinalization = Integer.MIN_VALUE;
	}

	@Override
	public void storeData(String date) throws SQLException {
		RowToAdd row = _tables[0].insert();
		row.setValue("date", date);
		row.setValue("total_mean", _totalAll / _i);
		row.setValue("total_min", _minAll);
		row.setValue("total_max", _maxAll);
		row.setValue("heap_mean", _totalHeap / _i);
		row.setValue("heap_min", _minHeap);
		row.setValue("heap_max", _maxHeap);
		row.setValue("non_heap_mean", _totalNonHeap / _i);
		row.setValue("non_heap_min", _minNonHeap);
		row.setValue("non_heap_max", _maxNonHeap);
		row.setValue("finalization_mean", ((float) _totalFinalization) / _i);
		row.setValue("finalization_min", _minNonHeap);
		row.setValue("finalization_max", _maxNonHeap);
		getServerMonitor().getSQLBridge().executeAsync(row.getStatement());
	}

	@Override
	public Table[] getTables() {
		return _tables;
	}
}