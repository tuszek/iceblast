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
public class ConnectionPingClientMessage extends ClientMessage implements
		ClientMessageFlags {

	private long mTimestamp;

	@Deprecated
	public ConnectionPingClientMessage() {

	}

	public long getTimestamp() {
		return this.mTimestamp;
	}

	public void setTimestamp(final long pTimestamp) {
		this.mTimestamp = pTimestamp;
	}

	@Override
	public short getFlag() {
		return FLAG_MESSAGE_CLIENT_CONNECTION_PING;
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
