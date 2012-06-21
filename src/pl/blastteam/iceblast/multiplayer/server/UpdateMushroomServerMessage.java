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
public class UpdateMushroomServerMessage extends ServerMessage implements
		IceBlastConstants {
	// ===========================================================
	// Fields
	// ===========================================================

	public int mPlayerID;
	public float mX;
	public float mY;

	// ===========================================================
	// Constructors
	// ===========================================================

	public UpdateMushroomServerMessage() {

	}

	public UpdateMushroomServerMessage(final int pPlayerID, final float pX,
			final float pY) {
		this.mPlayerID = pPlayerID;
		this.mX = pX;
		this.mY = pY;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void set(final int pPlayerID, final float pX, final float pY) {
		this.mPlayerID = pPlayerID;
		this.mX = pX;
		this.mY = pY;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public short getFlag() {
		return FLAG_MESSAGE_SERVER_UPDATE_MUSHROOM;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.mPlayerID = pDataInputStream.readInt();
		this.mX = pDataInputStream.readFloat();
		this.mY = pDataInputStream.readFloat();
	}

	@Override
	protected void onWriteTransmissionData(
			final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeInt(this.mPlayerID);
		pDataOutputStream.writeFloat(this.mX);
		pDataOutputStream.writeFloat(this.mY);
	}
}