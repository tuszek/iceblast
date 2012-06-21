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
public class ConnectionEstablishClientMessage extends ClientMessage implements
		ClientMessageFlags {

	private short mProtocolVersion;

	@Deprecated
	public ConnectionEstablishClientMessage() {

	}

	public ConnectionEstablishClientMessage(final short pProtocolVersion) {
		this.mProtocolVersion = pProtocolVersion;
	}

	public short getProtocolVersion() {
		return this.mProtocolVersion;
	}

	@Override
	public short getFlag() {
		return ClientMessageFlags.FLAG_MESSAGE_CLIENT_CONNECTION_ESTABLISH;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream)
			throws IOException {
		this.mProtocolVersion = pDataInputStream.readShort();
	}

	@Override
	protected void onWriteTransmissionData(
			final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeShort(this.mProtocolVersion);
	}

}
