package pl.blastteam.iceblast;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.extension.multiplayer.protocol.util.WifiUtils;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import pl.blastteam.iceblast.adt.messages.server.ConnectionCloseServerMessage;
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
public class MultiLan2 extends SimpleBaseGameActivity implements
		IceBlastConstants, IOnSceneTouchListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private int mUserID = USER_NOT_SET;

	// sprawdza czy byla kolizja, jak 0 to nie, jak jakas inna liczba to byla z
	// czyms kolizja
	private int collider = 0;

	// ilosc ptk potrzebnych aby wygrac
	private final int maxScore = 10;

	// ===========================================================
	// Fields
	// ===========================================================

	// dzwieki
	private Sound mRollerSound;

	private Score scores;

	private float myMushroomPositionX;
	private float myMushroomPositionY;

	private Camera mCamera;

	private String mServerIP = LOCALHOST_IP;
	private LanServer mServer;
	private LanServerConnector mServerConnector;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private BitmapTextureAtlas mBitmapTextureAtlasBgd;

	private ITextureRegion mBackgroundTextureRegion;
	private ITextureRegion mOpponentCircleFaceTextureRegion;
	private ITextureRegion mPlayerCircleFaceTextureRegion;
	private ITextureRegion mCirclePlayingBall;

	private Sprite mRoller;
	private Sprite mPlayerMushroom;
	private Sprite mOpponentMushroom;

	private Scene mScene;

	private PhysicsWorld mPhysicsWorld;

	private Font mScoreFont;
	private int SCORE_FONT_SIZE;
	// liczy laczna ilosc ptk
	private int mScoreCounter;

	private Font mWinFont;
	private Text mWinnerText;

	// zmienne od scian przy bramkach aby dalo sie do nich odwolac
	private Rectangle left_ground;
	private Rectangle right_ground;
	private Rectangle left_roof;
	private Rectangle right_roof;

	// Score text
	private Text opponentScore;
	private Text myScore;

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		this.mCamera.setCenter(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);

		EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
		engineOptions.getAudioOptions().setNeedsSound(true);

		return engineOptions;
	}

	@Override
	public Engine onCreateEngine(final EngineOptions pEngineOptions) {
		this.showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);

		return new LimitedFPSEngine(pEngineOptions, FPS);
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		// zwiêkszona szczegó³owoœæ na
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(
				this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.mBitmapTextureAtlasBgd = new BitmapTextureAtlas(
				this.getTextureManager(), 480, 800, TextureOptions.BILINEAR);

		this.mCirclePlayingBall = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"playing_ball_64x64.png", 0, 0);
		// wersja zoptymalizowana z tego co widze textureregion dziala tak samo
		// jak megatekstury aka atlas to 1 obrazek i na nim w pozycjach x i y
		// umieszcza sie nasze mniejsze tekstury
		this.mOpponentCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"mallet_1_64x64.png", 64, 0);
		this.mPlayerCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"mallet_1_64x64.png", 64, 0);
		this.mBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlasBgd, this,
						"game_background_1.png", 0, 0);

		this.mBitmapTextureAtlas.load();
		this.mBitmapTextureAtlasBgd.load();

		// loading font for scoring
		this.SCORE_FONT_SIZE = 32;
		final ITexture scoreFontTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		FontFactory.setAssetBasePath("font/");
		this.mScoreFont = FontFactory.createFromAsset(this.getFontManager(),
				scoreFontTexture, this.getAssets(), "Plok.ttf",
				SCORE_FONT_SIZE, true, Color.WHITE);
		this.mScoreFont.load();
		// czcionka dla tekstu po wygraniu meczu
		final ITexture winscreenFontTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.mWinFont = FontFactory.createFromAsset(this.getFontManager(),
				winscreenFontTexture, this.getAssets(), "Droid.ttf",
				SCORE_FONT_SIZE, true, Color.BLACK);
		this.mWinFont.load();

		// tworzenie dzwiekow
		SoundFactory.setAssetBasePath("mfx/");
		try {
			this.mRollerSound = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), this, "bip.mp3");
		} catch (final IOException e) {
			Debug.e(e);
		}
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		// dodane te 2 komendy z multitouchexample nie wiem co robia ale u mnie
		// podzialalo (chyba x])
		this.mScene.setOnAreaTouchTraversalFrontToBack();
		this.mScene.setTouchAreaBindingOnActionDownEnabled(true);

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		this.mPhysicsWorld.createBody(new BodyDef());

		final VertexBufferObjectManager vertexBufferObjectManager = this
				.getVertexBufferObjectManager();
		final Sprite bgd = new Sprite(0, 0, this.mBackgroundTextureRegion,
				this.getVertexBufferObjectManager());
		// bgd.setScale(0.2f);
		bgd.setHeight(CAMERA_HEIGHT);
		bgd.setWidth(CAMERA_WIDTH);
		this.mScene.attachChild(bgd);

		left_ground = new Rectangle(0, CAMERA_HEIGHT - 2,
				(float) (CAMERA_WIDTH * 0.2), 2, vertexBufferObjectManager);
		left_ground.setColor(0.5f, 0.5f, 0.5f);
		right_ground = new Rectangle((float) (CAMERA_WIDTH * 0.8),
				CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		right_ground.setColor(0.5f, 0.5f, 0.5f);
		left_roof = new Rectangle(0, 0, (float) (CAMERA_WIDTH * 0.2), 2,
				vertexBufferObjectManager);
		left_roof.setColor(0.5f, 0.5f, 0.5f);
		right_roof = new Rectangle((float) (CAMERA_WIDTH * 0.8), 0,
				CAMERA_WIDTH, 2, vertexBufferObjectManager);
		right_roof.setColor(0.5f, 0.5f, 0.5f);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT,
				vertexBufferObjectManager);
		left.setColor(0.5f, 0.5f, 0.5f);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2,
				CAMERA_HEIGHT, vertexBufferObjectManager);
		right.setColor(0.5f, 0.5f, 0.5f);

		Line middleLine = new Line(0, CAMERA_HEIGHT / 2, CAMERA_WIDTH,
				CAMERA_HEIGHT / 2, vertexBufferObjectManager);
		this.mScene.attachChild(middleLine);
		// scoring
		opponentScore = new Text(CAMERA_WIDTH / 2 - SCORE_FONT_SIZE / 2,
				(float) (CAMERA_HEIGHT / 2 - CAMERA_HEIGHT * 0.2)
						- SCORE_FONT_SIZE / 2 - 20, this.mScoreFont, "0", 2,
				vertexBufferObjectManager);
		myScore = new Text(CAMERA_WIDTH / 2 - SCORE_FONT_SIZE / 2,
				(float) (CAMERA_HEIGHT / 2 + CAMERA_HEIGHT * 0.2)
						- SCORE_FONT_SIZE / 2 + 20, this.mScoreFont, "0", 2,
				vertexBufferObjectManager);
		this.scores = new Score(myScore, opponentScore);
		this.mScene.attachChild(myScore);
		this.mScene.attachChild(opponentScore);

		this.mScene.attachChild(left_ground);
		this.mScene.attachChild(right_ground);
		this.mScene.attachChild(left_roof);
		this.mScene.attachChild(right_roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		this.addPlayerMushroom(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 3 * 2);
		this.addOpponentMushroom(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 3);
		this.addRoller(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);

		// to co jest pomiedzy dziurkami do blokowania cycka
		final Rectangle goal1 = new Rectangle((float) 0, 0,
				(float) CAMERA_WIDTH, 0, this.getVertexBufferObjectManager());
		final Rectangle goal2 = new Rectangle((float) 0, CAMERA_HEIGHT,
				(float) CAMERA_WIDTH, 0, this.getVertexBufferObjectManager());

		final Rectangle middle = new Rectangle(0, CAMERA_HEIGHT / 2,
				CAMERA_WIDTH, 0, this.getVertexBufferObjectManager());
		// kolor tla ustawiony na tych kwadratach
		goal1.setColor(0.09804f, 0.6274f, 0.8784f);
		goal2.setColor(0.09804f, 0.6274f, 0.8784f);
		this.mScene.attachChild(goal1);
		this.mScene.attachChild(goal2);
		this.mScene.attachChild(middle);

		// to co jest pomiedzy dziurkami do nabijania punktow
		final Rectangle roof = new Rectangle((float) 0, 0 - 45,
				(float) CAMERA_WIDTH, 0, this.getVertexBufferObjectManager());
		final Rectangle ground = new Rectangle((float) 0, CAMERA_HEIGHT + 45,
				(float) CAMERA_WIDTH, 0, this.getVertexBufferObjectManager());
		// kolor tla ustawiony na tych kwadratach
		roof.setColor(0.09804f, 0.6274f, 0.8784f);
		ground.setColor(0.09804f, 0.6274f, 0.8784f);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(ground);

		mScene.setOnSceneTouchListener(this);

		this.registerUpdateHandler(roof, ground);

		return this.mScene;
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "MutliLan.onDestroy");
		if (this.mServer != null) {
			try {
				this.mServer
						.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
			} catch (final IOException e) {
				Debug.e(e);
			}
			this.mServer.terminate();
		}

		if (this.mServerConnector != null) {
			this.mServerConnector.terminate();
		}

		super.onDestroy();
	}

	private void addPlayerMushroom(final float pX, final float pY) {
		Log.d(TAG, "MutliLan.addPlayerMushroom");
		mPlayerMushroom = new Sprite(pX, pY,
				this.mPlayerCircleFaceTextureRegion,
				this.getVertexBufferObjectManager());
		mPlayerMushroom.setScale(0.70f);
		this.mScene.attachChild(mPlayerMushroom);
	}

	private void addOpponentMushroom(final float pX, final float pY) {
		Log.d(TAG, "MutliLan.addOpponentMushroom");
		mOpponentMushroom = new Sprite(pX, pY,
				this.mOpponentCircleFaceTextureRegion,
				this.getVertexBufferObjectManager());
		mOpponentMushroom.setScale(0.70f);
		this.mScene.attachChild(mOpponentMushroom);
	}

	private void addRoller(final float pX, final float pY) {
		Log.d(TAG, "MutliLan.addRoller");
		mRoller = new Sprite(pX - Sprite.SPRITE_SIZE / 2, pY
				+ Sprite.SPRITE_SIZE / 2, this.mCirclePlayingBall,
				this.getVertexBufferObjectManager());
		mRoller.setScale(0.50f);
		this.mScene.attachChild(mRoller);
	}

	private void registerUpdateHandler(final Rectangle roof,
			final Rectangle ground) {
		Log.d(TAG, "MutliLan.registerUpdateHandler");
		this.mScene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() {
			}

			@Override
			public void onUpdate(final float pSecondsElapsed) {
				// wykrywa kolizje, jak tak to wydaje dzwiek
				if (mRoller.collidesWith(mPlayerMushroom)) {
					collider = 1;
				}
				if (mRoller.collidesWith(mOpponentMushroom)) {
					collider = 2;
				}
				if (mRoller.collidesWith(mPlayerMushroom) == false
						&& collider == 1) {
					collider = 0;
					mRollerSound.play();
				}
				;
				if (mRoller.collidesWith(mOpponentMushroom) == false
						&& collider == 2) {
					collider = 0;
					mRollerSound.play();
				}
				;

				// Send update
				if (mServerConnector != null) {
					try {
						if (mUserID != USER_NOT_SET)
							mServerConnector
									.sendClientMessage(new MoveMushroomClientMessage(
											mUserID, myMushroomPositionX,
											myMushroomPositionY));
					} catch (final IOException e) {
						Debug.e(e);
					}
				}
			}
		});
	}

	private void updateScore(final int pPlayerID, int score) {
		Log.d(TAG, "MutliLan.updateScore");
		this.scores.setClientScore(pPlayerID, score);

		mScoreCounter++;
		// wrzucenie wszystkiego w switche aby za duzej ifologii nie bylo
		switch (this.scores.getScore(pPlayerID)) {
		case 9:
			Text text = this.scores.getScoreText(pPlayerID);
			text.setPosition(text.getX() - SCORE_FONT_SIZE / 2, text.getY());
			break;
		case maxScore:
			winningEvent(pPlayerID);
			break;
		default:
			// sprawdza czy mozna dodac jakas przeszkode
			addObstacle(mScoreCounter);
			break;
		}
	}

	private void winningEvent(int pPlayerID) {
		// tworzy tekst
		final Rectangle okno1 = new Rectangle((CAMERA_WIDTH * 0.1f),
				(CAMERA_HEIGHT * 0.4f), (float) (CAMERA_WIDTH * 0.8f),
				(CAMERA_HEIGHT * 0.2f), this.getVertexBufferObjectManager());
		okno1.setColor(1f, 1f, 1f);
		final Rectangle okno2 = new Rectangle((CAMERA_WIDTH * 0.1f) + 3,
				(CAMERA_HEIGHT * 0.4f) + 3, (float) (CAMERA_WIDTH * 0.8f) - 6,
				(CAMERA_HEIGHT * 0.2f) - 6, this.getVertexBufferObjectManager());
		okno2.setColor(0.09804f, 0.6274f, 0.8784f);
		this.mWinnerText = new Text(0, 0, this.mWinFont,
				"Gratulacje!\nZwyciêzc¹ zosta³:\nGracz nr " + pPlayerID,
				new TextOptions(HorizontalAlign.CENTER),
				this.getVertexBufferObjectManager());
		this.mWinnerText.setScale(0.5f);
		this.mWinnerText
				.setPosition(
						(CAMERA_WIDTH * 0.5f - this.mWinnerText.getWidth() * 0.65f) * 0.5f,
						(CAMERA_HEIGHT * 0.5f + this.mWinnerText.getHeight() * 0.4f) * 0.5f);
		if (this.mUserID == OPPONENT_ID) {
			okno1.setRotation(180);
			okno2.setRotation(180);
			this.mWinnerText.setRotation(180);
		}
		this.mScene.attachChild(okno1);
		this.mScene.attachChild(okno2);
		this.mScene.attachChild(this.mWinnerText);

		// palzuje cala scene, mozna ja przywrocic przy pomocy start()
		this.mEngine.stop();

	}

	private void addObstacle(int points) {
		switch (points) {
		case 5:
			// tworzymy 2 bloki po bokach
			final Rectangle left_block = new Rectangle(0,
					(CAMERA_HEIGHT / 2) - 10, (float) (CAMERA_WIDTH * 0.2), 20,
					this.getVertexBufferObjectManager());
			final Rectangle right_block = new Rectangle(
					(float) (CAMERA_WIDTH * 0.8), CAMERA_HEIGHT / 2 - 10,
					CAMERA_WIDTH, 20, this.getVertexBufferObjectManager());
			// kolor tla ustawiony na tych kwadratach
			left_block.setColor(0.5f, 0.5f, 0.5f);
			right_block.setColor(0.5f, 0.5f, 0.5f);
			this.mScene.attachChild(left_block);
			this.mScene.attachChild(right_block);
			break;
		}
	}

	private void toast(final String pMessage) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MultiLan2.this, pMessage, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	private void initServerAndClient() {
		Log.d(TAG, "MutliLan.initServerAndClient");
		MultiLan2.this.initServer();

		try {
			Thread.sleep(500);
		} catch (final Throwable t) {
			Debug.e(t);
		}

		MultiLan2.this.initClient();
	}

	private void initServer() {
		Log.d(TAG, "MutliLan.initServer");
		this.mServer = new LanServer(new ExampleClientConnectorListener());
		this.mServer.start();

		this.mEngine.registerUpdateHandler(this.mServer);
	}

	private void initClient() {
		Log.d(TAG, "MutliLan.initClient");
		try {
			this.mServerConnector = new LanServerConnector(this.mServerIP,
					new ExampleServerConnectorListener());

			this.mServerConnector.getConnection().start();
		} catch (final Throwable t) {
			toast("Po³¹czenie z " + this.mServerIP
					+ " nie mog³o zostaæ nawi¹zane.");
			Debug.e(t);
		}
	}

	@Override
	protected Dialog onCreateDialog(final int pID) {
		Log.d(TAG, "MutliLan.onCreateDialog");
		switch (pID) {
		case DIALOG_SHOW_SERVER_IP_ID:
			Log.d(TAG, "MutliLan.onCreateDialog.DIALOG_SHOW_SERVER_IP_ID");
			try {
				return new AlertDialog.Builder(this)
						.setIcon(android.R.drawable.ic_dialog_info)
						.setTitle("Your Server-IP ...")
						.setCancelable(false)
						.setMessage(
								"Adres IP twojego serwera:\n"
										+ WifiUtils.getWifiIPv4Address(this))
						.setPositiveButton("Ok", null).create();
			} catch (final UnknownHostException e) {
				return new AlertDialog.Builder(this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle("IP Serwera ...")
						.setCancelable(false)
						.setMessage(
								"Wyst¹pi³ b³¹d przy próbie uzyskania adresu IP: "
										+ e)
						.setPositiveButton("Ok", new OnClickListener() {
							@Override
							public void onClick(final DialogInterface pDialog,
									final int pWhich) {
								MultiLan2.this.finish();
							}
						}).create();
			}
		case DIALOG_ENTER_SERVER_IP_ID:
			Log.d(TAG, "MutliLan.onCreateDialog.DIALOG_ENTER_SERVER_IP_ID");
			final EditText ipEditText = new EditText(this);
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle("WprowadŸ adres IP ...").setCancelable(false)
					.setView(ipEditText)
					.setPositiveButton("Po³¹cz", new OnClickListener() {
						@Override
						public void onClick(final DialogInterface pDialog,
								final int pWhich) {
							MultiLan2.this.mServerIP = ipEditText.getText()
									.toString();
							MultiLan2.this.initClient();
						}
					}).setNegativeButton("Anuluj", new OnClickListener() {
						@Override
						public void onClick(final DialogInterface pDialog,
								final int pWhich) {
							MultiLan2.this.finish();
						}
					}).create();
		case DIALOG_CHOOSE_SERVER_OR_CLIENT_ID:
			Log.d(TAG,
					"MutliLan.onCreateDialog.DIALOG_CHOOSE_SERVER_OR_CLIENT_ID");
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle("Chcesz byæ Serwerem czy Klientem ...")
					.setCancelable(false)
					.setPositiveButton("Klient", new OnClickListener() {
						@Override
						public void onClick(final DialogInterface pDialog,
								final int pWhich) {
							MultiLan2.this
									.showDialog(DIALOG_ENTER_SERVER_IP_ID);
						}
					}).setNeutralButton("Serwer", new OnClickListener() {
						@Override
						public void onClick(final DialogInterface pDialog,
								final int pWhich) {
							Log.d(TAG,
									"MutliLan.onCreateDialog.DIALOG_CHOOSE_SERVER_OR_CLIENT_ID.Server");
							MultiLan2.this.initServerAndClient();
							MultiLan2.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
						}
					}).create();
		default:
			Log.d(TAG, "MutliLan.onCreateDialog.default");
			return super.onCreateDialog(pID);
		}
	}

	private class LanServerConnector extends ServerConnector<SocketConnection>
			implements IceBlastConstants, ServerMessageFlags {
		// ===========================================================
		// Constructors
		// ===========================================================

		public LanServerConnector(
				final String pServerIP,
				final ISocketConnectionServerConnectorListener pSocketConnectionServerConnectorListener)
				throws IOException {
			super(new SocketConnection(new Socket(pServerIP, SERVER_PORT)),
					pSocketConnectionServerConnectorListener);

			this.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE,
					ConnectionCloseServerMessage.class,
					new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(
								final ServerConnector<SocketConnection> pServerConnector,
								final IServerMessage pServerMessage)
								throws IOException {
							MultiLan2.this.finish();
						}
					});

			this.registerServerMessage(
					FLAG_MESSAGE_SERVER_CONNECTION_ESTABLISHED,
					ConnectionEstablishedServerMessage.class,
					new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(
								final ServerConnector<SocketConnection> pServerConnector,
								final IServerMessage pServerMessage)
								throws IOException {
							Debug.d("CLIENT: Connection established.");
						}
					});

			this.registerServerMessage(
					FLAG_MESSAGE_SERVER_CONNECTION_REJECTED_PROTOCOL_MISSMATCH,
					ConnectionRejectedProtocolMissmatchServerMessage.class,
					new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(
								final ServerConnector<SocketConnection> pServerConnector,
								final IServerMessage pServerMessage)
								throws IOException {
							MultiLan2.this.finish();
						}
					});

			this.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_PONG,
					ConnectionPongServerMessage.class,
					new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(
								final ServerConnector<SocketConnection> pServerConnector,
								final IServerMessage pServerMessage)
								throws IOException {
							final ConnectionPongServerMessage connectionPongServerMessage = (ConnectionPongServerMessage) pServerMessage;
							final long roundtripMilliseconds = System
									.currentTimeMillis()
									- connectionPongServerMessage
											.getTimestamp();
							Debug.v("Ping: " + roundtripMilliseconds / 2 + "ms");
						}
					});

			this.registerServerMessage(FLAG_MESSAGE_SERVER_SET_MUSHROOMID,
					SetMushroomIDServerMessage.class,
					new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(
								final ServerConnector<SocketConnection> pServerConnector,
								final IServerMessage pServerMessage)
								throws IOException {
							final SetMushroomIDServerMessage setPaddleIDServerMessage = (SetMushroomIDServerMessage) pServerMessage;
							MultiLan2.this
									.setPlayerID(setPaddleIDServerMessage.mPlayerID);
						}
					});

			this.registerServerMessage(FLAG_MESSAGE_SERVER_UPDATE_SCORE,
					UpdateScoreServerMessage.class,
					new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(
								final ServerConnector<SocketConnection> pServerConnector,
								final IServerMessage pServerMessage)
								throws IOException {
							final UpdateScoreServerMessage updateScoreServerMessage = (UpdateScoreServerMessage) pServerMessage;
							MultiLan2.this.updateScore(
									updateScoreServerMessage.mPlayerID,
									updateScoreServerMessage.mScore);
						}
					});

			this.registerServerMessage(FLAG_MESSAGE_SERVER_UPDATE_BALL,
					UpdateRollerServerMessage.class,
					new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(
								final ServerConnector<SocketConnection> pServerConnector,
								final IServerMessage pServerMessage)
								throws IOException {
							final UpdateRollerServerMessage updateBallServerMessage = (UpdateRollerServerMessage) pServerMessage;
							MultiLan2.this.updateRoller(
									updateBallServerMessage.mX,
									updateBallServerMessage.mY);
						}
					});

			this.registerServerMessage(FLAG_MESSAGE_SERVER_UPDATE_MUSHROOM,
					UpdateMushroomServerMessage.class,
					new IServerMessageHandler<SocketConnection>() {
						@Override
						public void onHandleMessage(
								final ServerConnector<SocketConnection> pServerConnector,
								final IServerMessage pServerMessage)
								throws IOException {
							final UpdateMushroomServerMessage updatePaddleServerMessage = (UpdateMushroomServerMessage) pServerMessage;
							MultiLan2.this.updateMushroom(
									updatePaddleServerMessage.mPlayerID,
									updatePaddleServerMessage.mX,
									updatePaddleServerMessage.mY);
						}
					});

		}
	}

	private class ExampleServerConnectorListener implements
			ISocketConnectionServerConnectorListener {
		@Override
		public void onStarted(
				final ServerConnector<SocketConnection> pServerConnector) {
			MultiLan2.this.toast("KLIENT: Uzyskano po³¹czenie z Serwerem.");
		}

		@Override
		public void onTerminated(
				final ServerConnector<SocketConnection> pServerConnector) {
			MultiLan2.this.toast("KLIENT: Roz³¹czono z Serwera.");
			MultiLan2.this.finish();
		}
	}

	private class ExampleClientConnectorListener implements
			ISocketConnectionClientConnectorListener {
		@Override
		public void onStarted(
				final ClientConnector<SocketConnection> pClientConnector) {
			MultiLan2.this.toast("SERWER: Klient siê po³¹czy³: "
					+ pClientConnector.getConnection().getSocket()
							.getInetAddress().getHostAddress());
		}

		@Override
		public void onTerminated(
				final ClientConnector<SocketConnection> pClientConnector) {
			MultiLan2.this.toast("SERWER: Klient siê roz³¹czy³: "
					+ pClientConnector.getConnection().getSocket()
							.getInetAddress().getHostAddress());
		}
	}

	protected void setPlayerID(int mPlayerID) {
		if (mPlayerID == PLAYER_ID) {
			myMushroomPositionX = CAMERA_HEIGHT * 0.75f;
			myMushroomPositionY = CAMERA_WIDTH / 2;
		}
		if (mPlayerID == OPPONENT_ID) {
			mCamera.setRotation(180);
			myMushroomPositionX = CAMERA_HEIGHT * 0.25f;
			myMushroomPositionY = CAMERA_WIDTH / 2;
		}
		this.mUserID = mPlayerID;
		if (this.mUserID == OPPONENT_ID) {
			opponentScore.setRotation(180);
			myScore.setRotation(180);
		}
	}

	protected void updateMushroom(int mPlayerID, float mX, float mY) {
		if (mPlayerID == PLAYER_ID) {
			mPlayerMushroom.setPosition(mX - mPlayerMushroom.getHeight() / 2,
					mY - mPlayerMushroom.getWidth() / 2);
		}
		if (mPlayerID == OPPONENT_ID) {
			mOpponentMushroom.setPosition(mX - mOpponentMushroom.getHeight()
					/ 2, mY - mOpponentMushroom.getWidth() / 2);
		}
	}

	protected void updateRoller(float mX, float mY) {
		mRoller.setPosition(mX - mRoller.getHeight() / 2,
				mY - mRoller.getWidth() / 2);
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		this.myMushroomPositionX = pSceneTouchEvent.getY();
		this.myMushroomPositionY = pSceneTouchEvent.getX();
		return true;
	}
}
