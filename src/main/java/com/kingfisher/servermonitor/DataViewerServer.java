package com.kingfisher.servermonitor;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author KingFisher
 */
public final class DataViewerServer implements Closeable {

	private final ServerSocket _socket;
	private final DataViewerServerThread _thread = new DataViewerServerThread();
	private final String _password;
	private final SQLBridge _sql;

	public DataViewerServer(int port, String password, SQLBridge sql) throws IOException {
		_socket = new ServerSocket(port);
		_password = password;
		_sql = sql;
		_thread.start();
	}

	@Override
	public void close() throws IOException {
		_thread.interrupt();
		_socket.close();
	}

	private final class DataViewerServerThread extends Thread {

		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					new DataViewerSessionThread(_socket.accept()).start();
				} catch (IOException ex) {
					if (!isInterrupted()) {
						ServerMonitor.runtimeException("An unexpected error related to the data viewer occured.", ex);
					}
				}
			}
		}
	}

	private final class DataViewerSessionThread extends Thread {

		private static final float MB = 1024F * 1024F;

		private final Socket _socket;

		public DataViewerSessionThread(Socket socket) {
			_socket = socket;
		}

		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
				String request = reader.readLine();
				if (request != null) {
					if (request.startsWith("GET /favicon.ico HTTP/")) {
						sendResource("/favicon.ico");
					} else if (request.startsWith("GET /password.php?password=" + _password + " HTTP/")) {
						String file = getResourceAsString("/access.html");
						ResultSet result;
						StringBuilder builder;
						try {
							result = _sql.executeQuery("SELECT * FROM server_monitor_entity_count");
							builder = new StringBuilder();
							while (result.next()) {
								builder.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("mean")).append(", ").append(result.getInt("min")).append(", ").append(result.getInt("max")).append(']');
							}
							file = file.replace("//%%ENTITY_COUNT%%", builder.toString());
							result = _sql.executeQuery("SELECT * FROM server_monitor_player_count");
							builder = new StringBuilder();
							while (result.next()) {
								builder.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("mean")).append(", ").append(result.getInt("min")).append(", ").append(result.getInt("max")).append(']');
							}
							file = file.replace("//%%PLAYER_COUNT%%", builder.toString());
							result = _sql.executeQuery("SELECT * FROM server_monitor_redstone");
							builder = new StringBuilder();
							while (result.next()) {
								builder.append(", ['").append(result.getString("date")).append("', ").append(result.getInt("total")).append(']');
							}
							file = file.replace("//%%REDSTONE%%", builder.toString());
							result = _sql.executeQuery("SELECT * FROM server_monitor_pistons");
							builder = new StringBuilder();
							while (result.next()) {
								builder.append(", ['").append(result.getString("date")).append("', ").append(result.getInt("total")).append(']');
							}
							file = file.replace("//%%PISTONS%%", builder.toString());
							result = _sql.executeQuery("SELECT * FROM server_monitor_memory");
							builder = new StringBuilder();
							while (result.next()) {
								builder.append(", ['").append(result.getString("date")).append("', ").append((float) (result.getDouble("mean") / MB)).append(", ").append(result.getLong("min") / MB).append(", ").append(result.getLong("max") / MB).append(']');
							}
							file = file.replace("//%%MEMORY%%", builder.toString());
							result = _sql.executeQuery("SELECT * FROM server_monitor_tps");
							builder = new StringBuilder();
							while (result.next()) {
								builder.append(", ['").append(result.getString("date")).append("', ").append(result.getFloat("mean")).append(", ").append(result.getFloat("min")).append(", ").append(result.getFloat("max")).append(']');
							}
							file = file.replace("//%%TPS%%", builder.toString());
						} catch (SQLException ex) {
							ServerMonitor.runtimeException("An unexpected error related to the database, this can be due to the database, or the plugin itself.", ex);
						}
						sendString(file);
					} else if (request.startsWith("GET /password.php?password=")) {
						sendResource("/invalid.html");
					} else {
						sendResource("/password.html");
					}
				}
			} catch (IOException ex) {
				ServerMonitor.runtimeException("An unexpected error related to the data viewer occured.", ex);
			} finally {
				try {
					_socket.close();
				} catch (IOException ex) {
					ServerMonitor.runtimeException("An unexpected error related to the data viewer occured.", ex);
				}
			}
		}

		private void sendResource(String path) throws IOException {
			sendString(getResourceAsString(path));
		}

		private void sendString(String toSend) throws IOException {
			OutputStream writer = _socket.getOutputStream();
			writer.write(toSend.getBytes());
			writer.flush();
		}

		private String getResourceAsString(String path) throws IOException {
			InputStream resource = getClass().getResourceAsStream(path);
			StringBuilder builder = new StringBuilder();
			int c;
			while ((c = resource.read()) != -1) {
				builder.append((char) c);
			}
			return builder.toString();
		}
	}
}