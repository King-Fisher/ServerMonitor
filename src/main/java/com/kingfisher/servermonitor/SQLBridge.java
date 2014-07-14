package com.kingfisher.servermonitor;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author KingFisher
 */
public final class SQLBridge implements Closeable {

	private final ServerMonitor _serverMonitor;
	private final String _link;
	private final AtomicInteger _using = new AtomicInteger(0);
	private final AtomicReference<Connection> _connection = new AtomicReference<>();
	private final SQLThread _thread = new SQLThread();
	private final Queue<String> _toDo = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean _closed = new AtomicBoolean(false);

	public SQLBridge(ServerMonitor serverMonitor) throws SQLException {
		_serverMonitor = serverMonitor;
		_link = "jdbc:" + _serverMonitor.getConfiguration().getSQLType().toLowerCase() + "://" + _serverMonitor.getConfiguration().getSQLIP() + ":" + _serverMonitor.getConfiguration().getSQLPort();
		try (Connection connection = DriverManager.getConnection(_link, _serverMonitor.getConfiguration().getSQLUser(), _serverMonitor.getConfiguration().getSQLPassword())) {
			connection.createStatement().execute("CREATE DATABASE IF NOT EXISTS " + _serverMonitor.getConfiguration().getSQLDatabase());
		}
		_thread.start();
	}

	private void openConnection() throws SQLException {
		if (_using.getAndIncrement() == 0) {
			_connection.set(DriverManager.getConnection(_link, _serverMonitor.getConfiguration().getSQLUser(), _serverMonitor.getConfiguration().getSQLPassword()));
			executeNow("USE " + _serverMonitor.getConfiguration().getSQLDatabase());
		}
	}

	private void closeConnection() throws SQLException {
		if (_using.decrementAndGet() == 0) {
			_connection.get().close();
			_connection.set(null);
		}
	}

	/**
	 * Closes the bridge.
	 * @throws IOException 
	 */
	@Override
	public void close() throws IOException {
		waitReady();
		_thread.interrupt();
		_closed.set(true);
	}

	public boolean isOpen() throws SQLException {
		return !_closed.get() && _thread.isAlive();
	}

	public boolean isClosed() throws SQLException {
		return _closed.get() || !_thread.isAlive();
	}

	/**
	 * Waits until the bridge is ready (no waiting statements or connection closed).
	 */
	public void waitReady() {
		while (!_toDo.isEmpty() && _thread.isAlive()) {
		}
	}

	/**
	 * Executes the given statement asynchronously.
	 * @param statement
	 * @throws SQLException 
	 */
	public void executeAsync(String statement) throws SQLException {
		_toDo.add(statement);
	}

	/**
	 * Executes the given statement synchronously.
	 * @param statement
	 * @return 
	 * @throws SQLException 
	 */
	public boolean executeNow(String statement) throws SQLException {
		openConnection();
		boolean r = _connection.get().createStatement().execute(statement);
		closeConnection();
		return r;
	}

	/**
	 * Executes the given query statement synchronously.
	 * @param statement
	 * @return
	 * @throws SQLException 
	 */
	public ResultSet executeQuery(String statement) throws SQLException {
		openConnection();
		ResultSet r = _connection.get().createStatement().executeQuery(statement);
		closeConnection();
		return r;
	}

	/**
	 * Executes the given update statement synchronously.
	 * @param statement
	 * @return
	 * @throws SQLException 
	 */
	public int executeUpdate(String statement) throws SQLException {
		openConnection();
		int r = _connection.get().createStatement().executeUpdate(statement);
		closeConnection();
		return r;
	}

	private final class SQLThread extends Thread {

		@Override
		public final void run() {
			while (!isInterrupted()) {
				if (!_toDo.isEmpty()) {
					try {
						openConnection();
					} catch (SQLException ex) {
						_serverMonitor.runtimeException("An unexpected error occured while opening a SQL connection.", ex);
						return;
					}
					while (!_toDo.isEmpty()) {
						String statement = _toDo.remove();
						try {
							_connection.get().createStatement().execute(statement);
						} catch (SQLException ex) {
							_serverMonitor.runtimeException("An unexpected error occured while executing the following SQL statement: " + statement, ex);
						}
					}
					try {
						closeConnection();
					} catch (SQLException ex) {
						_serverMonitor.runtimeException("An unexpected error occured while closing a SQL connection.", ex);
						return;
					}
				}
			}
		}
	}
}