package pl.blastteam.iceblast.multiplayer.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import pl.blastteam.iceblast.IceBlastConstants;

/**
 * @author £ukasz Pogorzelski
 * @author Bart³omiej Tuszkowski
 * @author Tomasz Zaborowski
 * @version 2.0beta
 */
public class SetMushroomIDServerMessage extends ServerMessage implements
		IceBlastConstants {
	// ===========================================================
	// Fields
	// ===========================================================

	public int mPlayerID;

	// ===========================================================
	// Constructors
	// ===========================================================

	public SetMushroomIDServerMessage() {

	}

	public SetMushroomIDServerMessage(final int pPlayerID) {
		this.mPlayerID = pPlayerID;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void set(final int pPlayerID) {
		this.mPlayerID = pPlayerID;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public short getFlag() {
		return FLAG_MESSAGE_SERVER_SET_MUSHROOMID;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.mPlayerID = pDataInputStream.readInt();
	}

	@Override
	protected void onWriteTransmissionData(
			final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeInt(this.mPlayerID);
	}
}