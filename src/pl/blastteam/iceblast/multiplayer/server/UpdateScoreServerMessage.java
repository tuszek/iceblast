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
public class UpdateScoreServerMessage extends ServerMessage implements
		IceBlastConstants {
	// ===========================================================
	// Fields
	// ===========================================================

	public int mPlayerID;
	public int mScore;

	// ===========================================================
	// Constructors
	// ===========================================================

	public UpdateScoreServerMessage() {

	}

	public UpdateScoreServerMessage(final int pPlayerID, final int pScore) {
		this.mPlayerID = pPlayerID;
		this.mScore = pScore;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void set(final int pPlayerID, final int pScore) {
		this.mPlayerID = pPlayerID;
		this.mScore = pScore;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public short getFlag() {
		return FLAG_MESSAGE_SERVER_UPDATE_SCORE;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.mPlayerID = pDataInputStream.readInt();
		this.mScore = pDataInputStream.readInt();
	}

	@Override
	protected void onWriteTransmissionData(
			final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeInt(this.mPlayerID);
		pDataOutputStream.writeInt(this.mScore);
	}
}