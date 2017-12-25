package br.unesp.amoraes.dbvis.logic;

import br.unesp.amoraes.dbvis.logic.impl.ServerConnectionRest;

/**
 * A factory that returns the ServerConnection
 * @author Alessandro Moraes
 */
public class ServerConnectionFactory {
	public static ServerConnection create(){
		return ServerConnectionRest.getInstance();
	}
}
