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
public class ConnectionPongServerMessage extends ServerMessage implements
		ServerMessageFlags {

	private long mTimestamp;

	@Deprecated
	public ConnectionPongServerMessage() {

	}

	public ConnectionPongServerMessage(final long pTimestamp) {
		this.mTimestamp = pTimestamp;
	}

	public long getTimestamp() {
		return this.mTimestamp;
	}

	public void setTimestamp(long pTimestamp) {
		this.mTimestamp = pTimestamp;
	}

	@Override
	public short getFlag() {
		return FLAG_MESSAGE_SERVER_CONNECTION_PONG;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream)
			throws IOException {
		this.mTimestamp = pDataInputStream.readLong();
	}

	@Override
	protected void onWriteTransmissionData(
			final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeLong(this.mTimestamp);
	}
}
