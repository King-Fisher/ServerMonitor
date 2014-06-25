package com.kingfisher.servermonitor;

/**
 *
 * @author KingFisher
 */
public final class ServerMonitorConfigurationException extends Exception {

	public ServerMonitorConfigurationException(String reason) {
		super(reason);
	}

	public ServerMonitorConfigurationException(String reason, Throwable cause) {
		super(reason);
	}
}