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
public class ConnectionRejectedProtocolMissmatchServerMessage extends
		ServerMessage implements ServerMessageFlags {

	private short mProtocolVersion;

	@Deprecated
	public ConnectionRejectedProtocolMissmatchServerMessage() {

	}

	public ConnectionRejectedProtocolMissmatchServerMessage(
			final short pProtocolVersion) {
		this.mProtocolVersion = pProtocolVersion;
	}

	public short getProtocolVersion() {
		return this.mProtocolVersion;
	}

	public void setProtocolVersion(final short pProtocolVersion) {
		this.mProtocolVersion = pProtocolVersion;
	}

	@Override
	public short getFlag() {
		return FLAG_MESSAGE_SERVER_CONNECTION_REJECTED_PROTOCOL_MISSMATCH;
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
