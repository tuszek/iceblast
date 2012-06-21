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
public class UpdateRollerServerMessage extends ServerMessage implements
		IceBlastConstants {
	// ===========================================================
	// Fields
	// ===========================================================

	public float mX;
	public float mY;

	// ===========================================================
	// Constructors
	// ===========================================================

	public UpdateRollerServerMessage() {

	}

	public UpdateRollerServerMessage(final float pX, final float pY) {
		this.mX = pX;
		this.mY = pY;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void set(final float pX, final float pY) {
		this.mX = pX;
		this.mY = pY;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public short getFlag() {
		return FLAG_MESSAGE_SERVER_UPDATE_BALL;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.mX = pDataInputStream.readFloat();
		this.mY = pDataInputStream.readFloat();
	}

	@Override
	protected void onWriteTransmissionData(
			final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeFloat(this.mX);
		pDataOutputStream.writeFloat(this.mY);
	}
}