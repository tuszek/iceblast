package pl.blastteam.iceblast.adt.messages.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;

/**
 * @author £ukasz Pogorzelski
 * @author Bart³omiej Tuszkowski
 * @author Tomasz Zaborowski
 * @version 2.0beta
 */
public class ConnectionCloseClientMessage extends ClientMessage implements
		ClientMessageFlags {

	public ConnectionCloseClientMessage() {

	}

	@Override
	public short getFlag() {
		return FLAG_MESSAGE_CLIENT_CONNECTION_CLOSE;
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
