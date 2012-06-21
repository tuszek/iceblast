package pl.blastteam.iceblast.adt.messages.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

/**
 * @author £ukasz Pogorzelski
 * @author Bart³omiej Tuszkowski
 * @author Tomasz Zaborowski
 * @version 2.0beta
 */
public class ConnectionCloseServerMessage extends ServerMessage implements
		ServerMessageFlags {

	public ConnectionCloseServerMessage() {

	}

	@Override
	public short getFlag() {
		return FLAG_MESSAGE_SERVER_CONNECTION_CLOSE;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream)
			throws IOException {
		/* Nothing to read. */
	}

	@Override
	protected void onWriteTransmissionData(
			final DataOutputStream pDataOutputStream) throws IOException {
		/* Nothing to write. */
	}

}
