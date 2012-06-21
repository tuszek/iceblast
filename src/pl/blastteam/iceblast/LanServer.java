package pl.blastteam.iceblast;

import java.io.IOException;
import java.util.ArrayList;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.server.*;
import org.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener.DefaultSocketServerListener;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.util.adt.list.SmartList;
import org.andengine.util.debug.Debug;

import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

import android.util.SparseArray;
import pl.blastteam.iceblast.adt.messages.MessageConstants;
import pl.blastteam.iceblast.adt.messages.client.ClientMessageFlags;
import pl.blastteam.iceblast.adt.messages.client.ConnectionCloseClientMessage;
import pl.blastteam.iceblast.adt.messages.client.ConnectionEstablishClientMessage;
import pl.blastteam.iceblast.adt.messages.client.ConnectionPingClientMessage;
import pl.blastteam.iceblast.adt.messages.server.ConnectionEstablishedServerMessage;
import pl.blastteam.iceblast.adt.messages.server.ConnectionPongServerMessage;
import pl.blastteam.iceblast.adt.messages.server.ConnectionRejectedProtocolMissmatchServerMessage;
import pl.blastteam.iceblast.adt.messages.server.ServerMessageFlags;
import pl.blastteam.iceblast.multiplayer.client.MoveMushroomClientMessage;
import pl.blastteam.iceblast.multiplayer.server.SetMushroomIDServerMessage;
import pl.blastteam.iceblast.multiplayer.server.UpdateMushroomServerMessage;
import pl.blastteam.iceblast.multiplayer.server.UpdateRollerServerMessage;
import pl.blastteam.iceblast.multiplayer.server.UpdateScoreServerMessage;

/**
 * @author £ukasz Pogorzelski
 * @author Bart³omiej Tuszkowski
 * @author Tomasz Zaborowski
 * @version 2.0beta
 */
public class LanServer extends SocketServer<SocketConnectionClientConnector>
		implements IUpdateHandler, IceBlastConstants, ContactListener,
		ClientMessageFlags, ServerMessageFlags {
	// ===========================================================
	// Constants
	// ===========================================================

	// czy padla bramka
	private boolean gol = false;
	// czy gracz strzelil gola: jak tak to gracz, jak nie to przeciwnik
	private boolean mPlayerScore = false;

	// ===========================================================
	// Fields
	// ===========================================================

	// aby mozna bylo sie do niego odnosic poza metoda tworzaca plansze
	final VertexBufferObjectManager vertexBufferObjectManager;

	private Scene mScene;
	private MouseJoint mPlayerJoint;
	private MouseJoint mOpponentJoint;

	public static final FixtureDef WALL_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(0, 0.5f, 0.5f, false, CATEGORYBIT_WALL,
					MASKBITS_LAN_WALL, (short) 0);
	public static final FixtureDef GOAL_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(0, 0.5f, 0.5f, false, CATEGORYBIT_GOAL,
					MASKBITS_LAN_GOAL, (short) 0);
	public static final FixtureDef PADDLE_FIXTUREDEF = PhysicsFactory
			.createFixtureDef(1, 0.5f, 0.5f, false, CATEGORYBIT_SHROOM,
					MASKBITS_LAN_SHROOM, (short) 0);
	public static final FixtureDef BALL_FIXTUREDEF = PhysicsFactory
			.createFixtureDef(1, 0.5f, 0.5f, false, CATEGORYBIT_BALL,
					MASKBITS_LAN_BALL, (short) 0);
	public static final FixtureDef HIGH_DETECTOR_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(0, 0.5f, 0.5f, true, CATEGORYBIT_HIGH_DETECTOR,
					MASKBITS_LAN_HIGH_DETECTOR, (short) 0);
	public static final FixtureDef LOW_DETECTOR_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(0, 0.5f, 0.5f, true, CATEGORYBIT_LOW_DETECTOR,
					MASKBITS_LAN_LOW_DETECTOR, (short) 0);

	private final PhysicsWorld mPhysicsWorld;
	private final Body mRollerBody;
	private Body mGroundBody;
	private final SparseArray<Body> mMushroomBodies = new SparseArray<Body>();
	private Score scores = new Score();

	private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private final ArrayList<UpdateMushroomServerMessage> mUpdateMushroomServerMessages = new ArrayList<UpdateMushroomServerMessage>();

	// ===========================================================
	// Constructors
	// ===========================================================

	public LanServer(
			final ISocketConnectionClientConnectorListener pSocketConnectionClientConnectorListener) {
		super(
				SERVER_PORT,
				pSocketConnectionClientConnectorListener,
				new DefaultSocketServerListener<SocketConnectionClientConnector>());

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		// this.mScene.setOnAreaTouchTraversalFrontToBack();
		// this.mScene.setTouchAreaBindingOnActionDownEnabled(true);
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		this.mGroundBody = this.mPhysicsWorld.createBody(new BodyDef());

		this.vertexBufferObjectManager = new VertexBufferObjectManager();
		final Rectangle left_ground = new Rectangle(0, CAMERA_HEIGHT - 2,
				(float) (CAMERA_WIDTH * 0.2), 2, vertexBufferObjectManager);
		left_ground.setCullingEnabled(true);
		final Rectangle right_ground = new Rectangle(
				(float) (CAMERA_WIDTH * 0.8), CAMERA_HEIGHT - 2, CAMERA_WIDTH,
				2, vertexBufferObjectManager);
		right_ground.setCullingEnabled(true);
		final Rectangle left_roof = new Rectangle(0, 0,
				(float) (CAMERA_WIDTH * 0.2), 2, vertexBufferObjectManager);
		left_roof.setCullingEnabled(true);
		final Rectangle right_roof = new Rectangle(
				(float) (CAMERA_WIDTH * 0.8), 0, CAMERA_WIDTH, 2,
				vertexBufferObjectManager);
		right_roof.setCullingEnabled(true);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT,
				vertexBufferObjectManager);
		left.setCullingEnabled(true);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2,
				CAMERA_HEIGHT, vertexBufferObjectManager);
		right.setCullingEnabled(true);

		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left_ground,
				BodyType.StaticBody, WALL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right_ground,
				BodyType.StaticBody, WALL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left_roof,
				BodyType.StaticBody, WALL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right_roof,
				BodyType.StaticBody, WALL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left,
				BodyType.StaticBody, WALL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right,
				BodyType.StaticBody, WALL_FIXTURE_DEF);
		Line middleLine = new Line(0, CAMERA_HEIGHT / 2, CAMERA_WIDTH,
				CAMERA_HEIGHT / 2, vertexBufferObjectManager);
		middleLine.setCullingEnabled(true);

		this.mScene.attachChild(middleLine);
		this.mScene.attachChild(left_ground);
		this.mScene.attachChild(right_ground);
		this.mScene.attachChild(left_roof);
		this.mScene.attachChild(right_roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		// to co jest pomiedzy dziurkami do blokowania cycka
		final Rectangle goal1 = new Rectangle((float) 0, 0,
				(float) CAMERA_WIDTH, 0, vertexBufferObjectManager);
		goal1.setCullingEnabled(true);
		final Rectangle goal2 = new Rectangle((float) 0, CAMERA_HEIGHT,
				(float) CAMERA_WIDTH, 0, vertexBufferObjectManager);
		goal2.setCullingEnabled(true);
		final Rectangle middle = new Rectangle(0, CAMERA_HEIGHT / 2,
				CAMERA_WIDTH, 0, vertexBufferObjectManager);
		middle.setCullingEnabled(true);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, goal1,
				BodyType.StaticBody, GOAL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, goal2,
				BodyType.StaticBody, GOAL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, middle,
				BodyType.StaticBody, GOAL_FIXTURE_DEF);
		// kolor tla ustawiony na tych kwadratach
		goal1.setColor(0.09804f, 0.6274f, 0.8784f);
		goal2.setColor(0.09804f, 0.6274f, 0.8784f);
		this.mScene.attachChild(goal1);
		this.mScene.attachChild(goal2);
		this.mScene.attachChild(middle);
		// to co jest pomiedzy dziurkami do nabijania punktow
		final Rectangle roof = new Rectangle((float) 0, 0 - 45,
				(float) CAMERA_WIDTH, 0 + 1, vertexBufferObjectManager);
		roof.setCullingEnabled(true);
		final Rectangle ground = new Rectangle((float) 0, CAMERA_HEIGHT + 45,
				(float) CAMERA_WIDTH, 0 + 1, vertexBufferObjectManager);
		ground.setCullingEnabled(true);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof,
				BodyType.StaticBody, HIGH_DETECTOR_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground,
				BodyType.StaticBody, LOW_DETECTOR_FIXTURE_DEF);
		// kolor tla ustawiony na tych kwadratach
		roof.setColor(0.09804f, 0.6274f, 0.8784f);
		ground.setColor(0.09804f, 0.6274f, 0.8784f);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(ground);

		this.initMessagePool();

		this.mPhysicsWorld.setContactListener(this);

		this.mRollerBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld,
				0, 0, /* BALL_RADIUS */(float) 16, BodyType.DynamicBody,
				BALL_FIXTUREDEF);
		this.mRollerBody.setTransform(CAMERA_WIDTH
				/ (2 * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT),
				CAMERA_HEIGHT
						/ (2 * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT),
				0);

		final Body mMushroomBodyPlayer = PhysicsFactory.createCircleBody(
				this.mPhysicsWorld, CAMERA_WIDTH / 2, CAMERA_HEIGHT / 3 * 2,
				(float) (32 * 0.7), BodyType.DynamicBody, PADDLE_FIXTUREDEF);
		mMushroomBodyPlayer.setTransform((CAMERA_WIDTH / 2)
				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
				(CAMERA_HEIGHT * 0.75f)
						/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		this.mMushroomBodies.put(PLAYER_ID, mMushroomBodyPlayer);
		mMushroomBodyPlayer.setUserData("PLAYER");

		// dodany mousejoint do grzybka
		final Vector2 localPoint = Vector2Pool.obtain(0, 0);
		MouseJointDef mPlayerJointDef = new MouseJointDef();
		mPlayerJointDef.bodyA = this.mGroundBody;
		mPlayerJointDef.bodyB = mMushroomBodyPlayer;
		mPlayerJointDef.dampingRatio = 1;
		mPlayerJointDef.frequencyHz = 30;
		mPlayerJointDef.maxForce = (2000.0f * mMushroomBodyPlayer.getMass());
		mPlayerJointDef.collideConnected = true;
		mPlayerJointDef.target.set(mMushroomBodyPlayer
				.getWorldPoint(localPoint));

		this.mPlayerJoint = (MouseJoint) this.mPhysicsWorld
				.createJoint(mPlayerJointDef);

		final Body mMushroomBodyOponnent = PhysicsFactory.createCircleBody(
				this.mPhysicsWorld, CAMERA_WIDTH / 2, CAMERA_HEIGHT / 3,
				(float) (32 * 0.7), BodyType.DynamicBody, PADDLE_FIXTUREDEF);
		mMushroomBodyOponnent.setTransform((CAMERA_WIDTH / 2)
				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
				(CAMERA_HEIGHT * 0.25f)
						/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		this.mMushroomBodies.put(OPPONENT_ID, mMushroomBodyOponnent);
		mMushroomBodyOponnent.setUserData("OPPONENT");

		// dodany mousejoint do grzybka
		MouseJointDef mOpponentJointDef = new MouseJointDef();
		mOpponentJointDef.bodyA = this.mGroundBody;
		mOpponentJointDef.bodyB = mMushroomBodyOponnent;
		mOpponentJointDef.dampingRatio = 1;
		mOpponentJointDef.frequencyHz = 30;
		mOpponentJointDef.maxForce = (2000.0f * mMushroomBodyOponnent.getMass());
		mOpponentJointDef.collideConnected = true;
		mOpponentJointDef.target.set(mMushroomBodyOponnent
				.getWorldPoint(localPoint));
		Vector2Pool.recycle(localPoint);
		this.mOpponentJoint = (MouseJoint) this.mPhysicsWorld
				.createJoint(mOpponentJointDef);

		scores = new Score();
	}

	private void initMessagePool() {
		this.mMessagePool.registerMessage(
				FLAG_MESSAGE_SERVER_CONNECTION_ESTABLISHED,
				UpdateScoreServerMessage.class);
		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_CONNECTION_PONG,
				UpdateScoreServerMessage.class);
		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE,
				UpdateScoreServerMessage.class);
		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_UPDATE_SCORE,
				UpdateScoreServerMessage.class);
		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_UPDATE_BALL,
				UpdateRollerServerMessage.class);
		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_UPDATE_MUSHROOM,
				UpdateMushroomServerMessage.class);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void preSolve(final Contact pContact, final Manifold pManifold) {

	}

	@Override
	public void postSolve(final Contact pContact,
			final ContactImpulse pContactImpulse) {

	}

	@Override
	public void beginContact(final Contact pContact) {
		final Fixture fixtureA = pContact.getFixtureA();
		final Fixture fixtureB = pContact.getFixtureB();

		// kolizja z dolnym detektorem
		if ((fixtureB.getFilterData().categoryBits == 32)
				&& (fixtureA.getFilterData().categoryBits == 4)) {
			mPlayerScore = false;
			gol = true;
		} else if ((fixtureB.getFilterData().categoryBits == 4)
				&& (fixtureA.getFilterData().categoryBits == 32)) {
			mPlayerScore = false;
			gol = true;
		}

		// kolizja z gornym detektorem
		if ((fixtureB.getFilterData().categoryBits == 16)
				&& (fixtureA.getFilterData().categoryBits == 4)) {
			mPlayerScore = true;
			gol = true;
		} else if ((fixtureB.getFilterData().categoryBits == 4)
				&& (fixtureA.getFilterData().categoryBits == 16)) {
			mPlayerScore = true;
			gol = true;
		}

		if (gol == true) {
			final UpdateScoreServerMessage updateScoreServerMessage = (UpdateScoreServerMessage) this.mMessagePool
					.obtainMessage(FLAG_MESSAGE_SERVER_UPDATE_SCORE);
			if (mPlayerScore == false) {
				scores.increaseServerOpponentScore();
				updateScoreServerMessage.set(OPPONENT_ID,
						scores.getOpponentScore());
			}
			if (mPlayerScore == true) {
				scores.increaseServerPlayerScore();
				updateScoreServerMessage
						.set(PLAYER_ID, scores.getPlayerScore());
			}

			final SmartList<SocketConnectionClientConnector> clientConnectors = this.mClientConnectors;
			for (int i = 0; i < clientConnectors.size(); i++) {
				try {
					final ClientConnector<SocketConnection> clientConnector = clientConnectors
							.get(i);
					clientConnector.sendServerMessage(updateScoreServerMessage);
				} catch (final IOException e) {
					Debug.e(e);
				}
			}
			this.mMessagePool.recycleMessage(updateScoreServerMessage);
		}
	}

	@Override
	public void endContact(final Contact pContact) {
	}

	@Override
	public void onUpdate(final float pSecondsElapsed) {
		if (gol == true) {
			try {
				createObstacles(scores.getScoreCount());

				final SparseArray<Body> paddleBodies = this.mMushroomBodies;
				for (int j = 0; j < paddleBodies.size(); j++) {

					final int paddleID = paddleBodies.keyAt(j);
					final Body paddleBody = paddleBodies.get(paddleID);
					if (paddleID == PLAYER_ID) {
						paddleBody.setLinearVelocity(0, 0);
						paddleBody
								.setTransform(
										(CAMERA_WIDTH / 2)
												/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
										(CAMERA_HEIGHT * 0.75f)
												/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
										0);
					}
					if (paddleID == OPPONENT_ID) {
						paddleBody.setLinearVelocity(0, 0);
						paddleBody
								.setTransform(
										(CAMERA_WIDTH / 2)
												/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
										(CAMERA_HEIGHT * 0.25f)
												/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
										0);
					}
				}
				gol = false;
				this.mRollerBody.setLinearVelocity(0, 0);
				this.mRollerBody
						.setTransform(
								CAMERA_WIDTH
										/ (2 * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT),
								CAMERA_HEIGHT
										/ (2 * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT),
								0);
			} catch (final Exception e) {
				Debug.e(e);
			}
		}

		final Vector2 ballPosition = this.mRollerBody.getPosition();
		final float ballX = ballPosition.x
				* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
		final float ballY = ballPosition.y
				* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

		final UpdateRollerServerMessage updateBallServerMessage = (UpdateRollerServerMessage) this.mMessagePool
				.obtainMessage(FLAG_MESSAGE_SERVER_UPDATE_BALL);
		updateBallServerMessage.set(ballX, ballY);

		final ArrayList<UpdateMushroomServerMessage> updatePaddleServerMessages = this.mUpdateMushroomServerMessages;

		/* Prepare UpdatePaddleServerMessages. */
		final SparseArray<Body> paddleBodies = this.mMushroomBodies;
		for (int j = 0; j < paddleBodies.size(); j++) {

			final int paddleID = paddleBodies.keyAt(j);
			final Body paddleBody = paddleBodies.get(paddleID);
			final Vector2 paddlePosition = paddleBody.getPosition();

			final float paddleX = paddlePosition.x
					* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
			final float paddleY = paddlePosition.y
					* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

			final UpdateMushroomServerMessage updatePaddleServerMessage = (UpdateMushroomServerMessage) this.mMessagePool
					.obtainMessage(FLAG_MESSAGE_SERVER_UPDATE_MUSHROOM);
			updatePaddleServerMessage.set(paddleID, paddleX, paddleY);

			updatePaddleServerMessages.add(updatePaddleServerMessage);
		}

		try {
			this.sendBroadcastServerMessage(updateBallServerMessage);

			for (int j = 0; j < mUpdateMushroomServerMessages.size(); j++) {
				this.sendBroadcastServerMessage(mUpdateMushroomServerMessages
						.get(j));
			}
			this.sendBroadcastServerMessage(updateBallServerMessage);
		} catch (final IOException e) {
			Debug.e(e);
		}

		/* Recycle messages. */
		this.mMessagePool.recycleMessage(updateBallServerMessage);
		this.mMessagePool.recycleMessages(mUpdateMushroomServerMessages);
		mUpdateMushroomServerMessages.clear();

		// przesuniete na koniec wtedy sie az tak nie wiesza (obstawiam ze
		// wywolywanie tego przed)
		// wysylaniem komunikatow zapychalo liste albo cos
		this.mPhysicsWorld.onUpdate(pSecondsElapsed);
	}

	@Override
	public void reset() {

	}

	@Override
	protected SocketConnectionClientConnector newClientConnector(
			final SocketConnection pSocketConnection) throws IOException {
		final SocketConnectionClientConnector clientConnector = new SocketConnectionClientConnector(
				pSocketConnection);

		clientConnector.registerClientMessage(
				FLAG_MESSAGE_CLIENT_MOVE_MUSHROOM,
				MoveMushroomClientMessage.class,
				new IClientMessageHandler<SocketConnection>() {
					@Override
					public void onHandleMessage(
							final ClientConnector<SocketConnection> pClientConnector,
							final IClientMessage pClientMessage)
							throws IOException {
						final MoveMushroomClientMessage moveMushroomClientMessage = (MoveMushroomClientMessage) pClientMessage;
						final Body mushroomBody = LanServer.this.mMushroomBodies
								.get(moveMushroomClientMessage.mPlayerID);

						final Vector2 vecp = Vector2Pool
								.obtain(moveMushroomClientMessage.mX
										/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
										moveMushroomClientMessage.mY
												/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
						if (moveMushroomClientMessage.mPlayerID == PLAYER_ID)
							mPlayerJoint.setTarget(vecp);
						else
							mOpponentJoint.setTarget(vecp);
						Vector2Pool.recycle(vecp);
						mushroomBody.setLinearVelocity(0, 0);

					}
				});

		clientConnector.registerClientMessage(
				FLAG_MESSAGE_CLIENT_CONNECTION_CLOSE,
				ConnectionCloseClientMessage.class,
				new IClientMessageHandler<SocketConnection>() {
					@Override
					public void onHandleMessage(
							final ClientConnector<SocketConnection> pClientConnector,
							final IClientMessage pClientMessage)
							throws IOException {
						scores.resetServerScore();
						pClientConnector.terminate();
					}
				});

		clientConnector.registerClientMessage(
				FLAG_MESSAGE_CLIENT_CONNECTION_ESTABLISH,
				ConnectionEstablishClientMessage.class,
				new IClientMessageHandler<SocketConnection>() {
					@Override
					public void onHandleMessage(
							final ClientConnector<SocketConnection> pClientConnector,
							final IClientMessage pClientMessage)
							throws IOException {
						final ConnectionEstablishClientMessage connectionEstablishClientMessage = (ConnectionEstablishClientMessage) pClientMessage;
						if (connectionEstablishClientMessage
								.getProtocolVersion() == MessageConstants.PROTOCOL_VERSION) {
							final ConnectionEstablishedServerMessage connectionEstablishedServerMessage = (ConnectionEstablishedServerMessage) LanServer.this.mMessagePool
									.obtainMessage(FLAG_MESSAGE_SERVER_CONNECTION_ESTABLISHED);
							try {
								pClientConnector
										.sendServerMessage(connectionEstablishedServerMessage);
							} catch (IOException e) {
								Debug.e(e);
							}
							LanServer.this.mMessagePool
									.recycleMessage(connectionEstablishedServerMessage);
						} else {
							final ConnectionRejectedProtocolMissmatchServerMessage connectionRejectedProtocolMissmatchServerMessage = (ConnectionRejectedProtocolMissmatchServerMessage) LanServer.this.mMessagePool
									.obtainMessage(FLAG_MESSAGE_SERVER_CONNECTION_REJECTED_PROTOCOL_MISSMATCH);
							connectionRejectedProtocolMissmatchServerMessage
									.setProtocolVersion(MessageConstants.PROTOCOL_VERSION);
							try {
								pClientConnector
										.sendServerMessage(connectionRejectedProtocolMissmatchServerMessage);
							} catch (IOException e) {
								Debug.e(e);
							}
							LanServer.this.mMessagePool
									.recycleMessage(connectionRejectedProtocolMissmatchServerMessage);
						}
					}
				});

		clientConnector.registerClientMessage(
				FLAG_MESSAGE_CLIENT_CONNECTION_PING,
				ConnectionPingClientMessage.class,
				new IClientMessageHandler<SocketConnection>() {
					@Override
					public void onHandleMessage(
							final ClientConnector<SocketConnection> pClientConnector,
							final IClientMessage pClientMessage)
							throws IOException {
						final ConnectionPongServerMessage connectionPongServerMessage = (ConnectionPongServerMessage) LanServer.this.mMessagePool
								.obtainMessage(FLAG_MESSAGE_SERVER_CONNECTION_PONG);
						try {
							pClientConnector
									.sendServerMessage(connectionPongServerMessage);
						} catch (IOException e) {
							Debug.e(e);
						}
						LanServer.this.mMessagePool
								.recycleMessage(connectionPongServerMessage);
					}
				});

		clientConnector.sendServerMessage(new SetMushroomIDServerMessage(
				this.mClientConnectors.size() + 1));
		return clientConnector;

	}

	private void createObstacles(int scoreCount) {
		switch (scoreCount) {
		case 5:
			// tworzymy 2 bloki po bokach
			final Rectangle left_block = new Rectangle(0,
					(CAMERA_HEIGHT / 2) - 10, (float) (CAMERA_WIDTH * 0.2), 20,
					this.vertexBufferObjectManager);
			final Rectangle right_block = new Rectangle(
					(float) (CAMERA_WIDTH * 0.8), CAMERA_HEIGHT / 2 - 10,
					CAMERA_WIDTH, 20, this.vertexBufferObjectManager);
			PhysicsFactory.createBoxBody(this.mPhysicsWorld, left_block,
					BodyType.StaticBody, WALL_FIXTURE_DEF);
			PhysicsFactory.createBoxBody(this.mPhysicsWorld, right_block,
					BodyType.StaticBody, WALL_FIXTURE_DEF);
			// kolor tla ustawiony na tych kwadratach
			left_block.setCullingEnabled(true);
			right_block.setCullingEnabled(true);
			this.mScene.attachChild(left_block);
			this.mScene.attachChild(right_block);
			break;
		default:
			break;
		}

	}
}