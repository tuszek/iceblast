package pl.blastteam.iceblast.multiplayer.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;

import pl.blastteam.iceblast.IceBlastConstants;

/**
 * @author £ukasz Pogorzelski
 * @author Bart³omiej Tuszkowski
 * @author Tomasz Zaborowski
 * @version 2.0beta
 */
public class MoveMushroomClientMessage extends ClientMessage implements
		IceBlastConstants {
	// ===========================================================
	// Fields
	// ===========================================================

	public int mPlayerID;
	public float mY;
	public float mX;

	// ===========================================================
	// Constructors
	// ===========================================================

	public MoveMushroomClientMessage() {

	}

	public MoveMushroomClientMessage(final int pPlayerID, final float pY,
			final float pX) {
		this.mPlayerID = pPlayerID;
		this.mY = pY;
		this.mX = pX;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setPaddleID(final int pPlayerID, final float pX, final float pY) {
		this.mPlayerID = pPlayerID;
		this.mY = pY;
		this.mX = pX;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public short getFlag() {
		return FLAG_MESSAGE_CLIENT_MOVE_MUSHROOM;
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