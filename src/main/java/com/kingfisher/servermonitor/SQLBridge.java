package com.kingfisher.servermonitor;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Hector
 */
public final class SQLBridge implements Closeable {

	private final Connection _connection;
	private final SQLThread _thread = new SQLThread();
	private final Queue<String> _toDo = new ConcurrentLinkedQueue<>();

	public SQLBridge(String type, String ip, int port, String user, String password) throws SQLException {
		_connection = DriverManager.getConnection("jdbc:" + type.toLowerCase() + "://" + ip + ":" + port, user, password);
		_thread.start();
	}

	/**
	 * Closes the bridge.
	 * @throws IOException 
	 */
	@Override
	public synchronized void close() throws IOException {
		waitReady();
		_thread.interrupt();
		try {
			_connection.close();
		} catch (SQLException ex) {
			throw new IOException(ex);
		}
	}

	/**
	 * Waits until the bridge is ready (no waiting statements or connection closed).
	 */
	public void waitReady() {
		while (!_toDo.isEmpty() && _thread.isAlive()) {
		}
	}

	/**
	 * Returns the SQL connection.
	 * @return 
	 */
	public Connection getConnection() {
		return _connection;
	}

	/**
	 * Executes the given statement asynchronously.
	 * @param statement
	 * @throws SQLException 
	 */
	public void executeAsync(String statement) throws SQLException {
		if (_connection.isClosed()) {
			throw new SQLException("Bridge closed.");
		} else {
			_toDo.add(statement);
		}
	}

	/**
	 * Executes the given statement synchronously.
	 * @param statement
	 * @return 
	 * @throws SQLException 
	 */
	public boolean executeNow(String statement) throws SQLException {
		if (_connection.isClosed()) {
			throw new SQLException("Bridge closed.");
		} else {
			return _connection.createStatement().execute(statement);
		}
	}

	/**
	 * Executes the given query statement synchronously.
	 * @param statement
	 * @return
	 * @throws SQLException 
	 */
	public ResultSet executeQuery(String statement) throws SQLException {
		if (_connection.isClosed()) {
			throw new SQLException("Bridge closed.");
		} else {
			return _connection.createStatement().executeQuery(statement);
		}
	}

	/**
	 * Executes the given update statement synchronously.
	 * @param statement
	 * @return
	 * @throws SQLException 
	 */
	public int executeUpdate(String statement) throws SQLException {
		if (_connection.isClosed()) {
			throw new SQLException("Bridge closed.");
		} else {
			return _connection.createStatement().executeUpdate(statement);
		}
	}

	private final class SQLThread extends Thread {

		@Override
		public final void run() {
			while (!isInterrupted()) {
				while (!_toDo.isEmpty()) {
					try {
						_connection.createStatement().execute(_toDo.remove());
					} catch (SQLException ex) {
						throw new RuntimeException("An unexpected error occured while executing a SQL statement, this can be due to the database, or the plugin itself. Please contact the developer if you think that the error is due to the plugin. The error message is: " + ex.getMessage(), ex);
					}
				}
			}
		}
	}
}