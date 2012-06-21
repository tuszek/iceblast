package pl.blastteam.iceblast;

import org.andengine.engine.camera.Camera;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.shape.IAreaShape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.controller.MultiTouch;
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

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

import android.graphics.Color;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

/**
 * @author £ukasz Pogorzelski
 * @author Bart³omiej Tuszkowski
 * @author Tomasz Zaborowski
 * @version 2.0beta
 */
public class Multitouch extends SimpleBaseGameActivity implements
		IceBlastConstants, ContactListener {
	// ===========================================================
	// Constants
	// ===========================================================

	// maksymalna ilosc ptk do zwyciestwa
	private final int maxScore = 10;
	// czy maja byc ustawione powerupy
	private final int powerups = 1;

	// ===========================================================
	// Fields
	// ===========================================================

	private Score scores;

	// dzwieki
	private Sound mRollerSound;
	private Sound mPowerupSound;

	private TimerHandler powerupTimerHandler;
	// tekstury
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private BitmapTextureAtlas mBitmapTextureAtlasBgd;

	private ITextureRegion mBackgroundTextureRegion;
	private ITextureRegion mOpponentCircleFaceTextureRegion;
	private ITextureRegion mPlayerCircleFaceTextureRegion;
	private ITextureRegion mCirclePlayingBall;
	private ITextureRegion mCirclePowerup;

	private Sprite mRoller;
	private Body mRollerBody;
	private Sprite mPlayerMushroom;
	private Sprite mOpponentMushroom;
	private Body mPlayerMushromBody;
	private Body mOpponentMushromBody;
	private Sprite mPowerup;

	public static final FixtureDef WALL_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(0, 0.5f, 0.5f, false, CATEGORYBIT_WALL,
					MASKBITS_WALL, (short) 0);
	public static final FixtureDef GOAL_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(0, 0.5f, 0.5f, false, CATEGORYBIT_GOAL,
					MASKBITS_GOAL, (short) 0);
	public static final FixtureDef SHROOM_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(1, 0.5f, 0.5f, false, CATEGORYBIT_SHROOM,
					MASKBITS_SHROOM, (short) 0);
	public static final FixtureDef BALL_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(1, 0.5f, 0.5f, false, CATEGORYBIT_BALL,
					MASKBITS_BALL, (short) 0);

	private Scene mScene;

	private PhysicsWorld mPhysicsWorld;

	private Font mScoreFont;
	private int SCORE_FONT_SIZE;
	// liczy laczna ilosc ptk
	private int mScoreCounter;

	private Font mWinFont;
	private Text mWinnerText;

	private MouseJoint mPlayerMouseJointActive;
	private MouseJoint mOpponentMouseJointActive;
	private Body mGroundBody;

	// zmienne od scian przy bramkach aby dalo sie do nich odwolac
	private Rectangle left_ground;
	private Body left_gBody;
	private Rectangle right_ground;
	private Body right_gBody;
	private Rectangle left_roof;
	private Body left_rBody;
	private Rectangle right_roof;
	private Body right_rBody;
	// obiekty blokujace bramke
	private Rectangle blocker_ground;
	private Body blocker_rBody;
	private Rectangle blocker_roof;
	private Body blocker_gBody;

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	private void toast(final String pMessage) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(Multitouch.this, pMessage, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	public EngineOptions onCreateEngineOptions() {

		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
		engineOptions.getAudioOptions().setNeedsSound(true);

		if (MultiTouch.isSupported(this)) {
			if (MultiTouch.isSupportedDistinct(this)) {
				Toast.makeText(this, "MultiTouch wykryty.", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(
						this,
						"MultiTouch wykryty, jednak mog¹ wyst¹piæ problemy ze sterowaniem.",
						Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(
					this,
					"Przykro nam, ale twoje urz¹dzenie nie posiada funkcji Multitouch. Zalecamy zakup nowego urz¹dzenia.",
					Toast.LENGTH_LONG).show();
		}
		return engineOptions;
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
		this.mCirclePowerup = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"powerup_64x64.png", 0, 64);

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
			this.mPowerupSound = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), this,
					"glass_breaking_2.wav");
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
		this.mScene.setOnAreaTouchTraversalFrontToBack();
		this.mScene.setTouchAreaBindingOnActionDownEnabled(true);

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		this.mGroundBody = this.mPhysicsWorld.createBody(new BodyDef());

		final VertexBufferObjectManager vertexBufferObjectManager = this
				.getVertexBufferObjectManager();
		final Sprite bgd = new Sprite(0, 0, this.mBackgroundTextureRegion,
				this.getVertexBufferObjectManager());
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
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2,
				CAMERA_HEIGHT, vertexBufferObjectManager);
		left.setColor(0.5f, 0.5f, 0.5f);
		right.setColor(0.5f, 0.5f, 0.5f);

		left_gBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld,
				left_ground, BodyType.StaticBody, WALL_FIXTURE_DEF);
		right_gBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld,
				right_ground, BodyType.StaticBody, WALL_FIXTURE_DEF);
		left_rBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld,
				left_roof, BodyType.StaticBody, WALL_FIXTURE_DEF);
		right_rBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld,
				right_roof, BodyType.StaticBody, WALL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left,
				BodyType.StaticBody, WALL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right,
				BodyType.StaticBody, WALL_FIXTURE_DEF);

		Line middleLine = new Line(0, CAMERA_HEIGHT / 2, CAMERA_WIDTH,
				CAMERA_HEIGHT / 2, vertexBufferObjectManager);
		this.mScene.attachChild(middleLine);
		// scoring
		final Text opponentScore = new Text(CAMERA_WIDTH / 2 - SCORE_FONT_SIZE
				/ 2, (float) (CAMERA_HEIGHT / 2 - CAMERA_HEIGHT * 0.2)
				- SCORE_FONT_SIZE / 2 - 20, this.mScoreFont, "0", 2,
				vertexBufferObjectManager);
		opponentScore.setRotation(180);
		final Text myScore = new Text(CAMERA_WIDTH / 2 - SCORE_FONT_SIZE / 2,
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
				(float) CAMERA_WIDTH, 4, this.getVertexBufferObjectManager());
		final Rectangle ground = new Rectangle((float) 0, CAMERA_HEIGHT + 45,
				(float) CAMERA_WIDTH, 4, this.getVertexBufferObjectManager());
		// kolor tla ustawiony na tych kwadratach
		roof.setColor(0.09804f, 0.6274f, 0.8784f);
		ground.setColor(0.09804f, 0.6274f, 0.8784f);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(ground);

		this.registerUpdateHandler(roof, ground);
		this.mPhysicsWorld.setContactListener(this);

		return this.mScene;
	}

	@Override
	public void onGameCreated() {
		if (powerups == 1) {
			// uruchomienie powerupow
			createPowerupsTimeHandler();
		}
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();

	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

	}

	@Override
	public void beginContact(final Contact pContact) {

	}

	@Override
	public void endContact(final Contact pContact) {
		final Fixture fixtureA = pContact.getFixtureA();
		final Fixture fixtureB = pContact.getFixtureB();

		if ((fixtureB.getFilterData().categoryBits == 4)
				|| (fixtureA.getFilterData().categoryBits == 4)) {
			this.mRollerSound.play();
		}
	}

	@Override
	public void preSolve(final Contact pContact, final Manifold pManifold) {

	}

	@Override
	public void postSolve(final Contact pContact,
			final ContactImpulse pContactImpulse) {

	}

	// ===========================================================
	// Methods
	// ===========================================================

	public MouseJoint createMouseJoint(final IAreaShape pFace,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		final Body body = (Body) pFace.getUserData();
		final MouseJointDef mouseJointDef = new MouseJointDef();
		final Vector2 localPoint = Vector2Pool.obtain(0, 0);

		mouseJointDef.bodyA = this.mGroundBody;
		mouseJointDef.bodyB = body;
		mouseJointDef.dampingRatio = 1;
		mouseJointDef.frequencyHz = 30;
		mouseJointDef.maxForce = (2000.0f * body.getMass());
		mouseJointDef.collideConnected = true;

		mouseJointDef.target.set(body.getWorldPoint(localPoint));
		Vector2Pool.recycle(localPoint);

		return (MouseJoint) this.mPhysicsWorld.createJoint(mouseJointDef);
	}

	private void addPlayerMushroom(final float pX, final float pY) {
		mPlayerMushroom = new Sprite(pX, pY,
				this.mPlayerCircleFaceTextureRegion,
				this.getVertexBufferObjectManager()) {
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {

				switch (pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					if (!mGrabbed) {
						mPlayerMouseJointActive = createMouseJoint(this,
								pTouchAreaLocalX, pTouchAreaLocalY);
						mGrabbed = true;
					}
					break;
				case TouchEvent.ACTION_MOVE:
					if (mGrabbed) {
						final Vector2 vecp = Vector2Pool
								.obtain(pSceneTouchEvent.getX()
										/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
										pSceneTouchEvent.getY()
												/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
						mPlayerMouseJointActive.setTarget(vecp);
						Vector2Pool.recycle(vecp);
					}
					break;
				case TouchEvent.ACTION_UP:
					if (mGrabbed) {
						// ponizsza komenda zatrzymuje grzybka kiedy zdejmiemy z
						// niego palec
						mPlayerMouseJointActive.getBodyB().setLinearVelocity(0,
								0);
						mPhysicsWorld.destroyJoint(mPlayerMouseJointActive);
						mPlayerMouseJointActive = null;
						mGrabbed = false;
					}
				}
				return true;
			}

		};
		mPlayerMushroom.setScale(0.70f);
		mPlayerMushromBody = PhysicsFactory.createCircleBody(
				this.mPhysicsWorld, mPlayerMushroom, BodyType.DynamicBody,
				SHROOM_FIXTURE_DEF);
		mPlayerMushromBody.setTransform((CAMERA_WIDTH / 2)
				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
				(CAMERA_HEIGHT * 0.75f)
						/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);

		mPlayerMushroom.setUserData(mPlayerMushromBody);

		this.mScene.registerTouchArea(mPlayerMushroom);
		this.mScene.attachChild(mPlayerMushroom);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				mPlayerMushroom, mPlayerMushromBody, true, true));
	}

	private void addOpponentMushroom(final float pX, final float pY) {
		mOpponentMushroom = new Sprite(pX, pY,
				this.mOpponentCircleFaceTextureRegion,
				this.getVertexBufferObjectManager()) {
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {

				switch (pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					if (!mGrabbed) {
						mOpponentMouseJointActive = createMouseJoint(this,
								pTouchAreaLocalX, pTouchAreaLocalY);
						mGrabbed = true;
					}
					break;
				case TouchEvent.ACTION_MOVE:
					if (mGrabbed) {
						final Vector2 vecp = Vector2Pool
								.obtain(pSceneTouchEvent.getX()
										/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
										pSceneTouchEvent.getY()
												/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
						mOpponentMouseJointActive.setTarget(vecp);
						Vector2Pool.recycle(vecp);
					}
					break;
				case TouchEvent.ACTION_UP:
					if (mGrabbed) {
						// ponizsza komenda zatrzymuje grzybka kiedy zdejmiemy z
						// niego palec
						mOpponentMouseJointActive.getBodyB().setLinearVelocity(
								0, 0);
						mPhysicsWorld.destroyJoint(mOpponentMouseJointActive);
						mOpponentMouseJointActive = null;
						mGrabbed = false;
					}
				}
				return true;
			}
		};
		mOpponentMushroom.setScale(0.70f);
		mOpponentMushromBody = PhysicsFactory.createCircleBody(
				this.mPhysicsWorld, mOpponentMushroom, BodyType.DynamicBody,
				SHROOM_FIXTURE_DEF);
		mOpponentMushromBody.setTransform((CAMERA_WIDTH / 2)
				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
				(CAMERA_HEIGHT * 0.25f)
						/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);

		mOpponentMushroom.setUserData(mOpponentMushromBody);

		this.mScene.registerTouchArea(mOpponentMushroom);
		this.mScene.attachChild(mOpponentMushroom);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				mOpponentMushroom, mOpponentMushromBody, true, true));
	}

	private void addRoller(final float pX, final float pY) {
		mRoller = new Sprite(pX - Sprite.SPRITE_SIZE / 2, pY
				+ Sprite.SPRITE_SIZE / 2, this.mCirclePlayingBall,
				this.getVertexBufferObjectManager());
		mRoller.setScale(0.50f);
		mRollerBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld,
				mRoller, BodyType.DynamicBody, BALL_FIXTURE_DEF);
		mRollerBody.setTransform((CAMERA_WIDTH / 2)
				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
				(CAMERA_HEIGHT / 2)
						/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);

		this.mScene.attachChild(mRoller);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				mRoller, mRollerBody, true, true));
	}

	private void registerUpdateHandler(final Rectangle roof,
			final Rectangle ground) {
		this.mScene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() {
			}

			@Override
			public void onUpdate(final float pSecondsElapsed) {
				// kolizja pilki z gora
				if (roof.collidesWith(mRoller)) {
					// zwiekszenie punktacji
					updateScore(PLAYER_ID);
					mRollerBody
							.setTransform(
									(CAMERA_WIDTH / 2)
											/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
									(CAMERA_HEIGHT / 2)
											/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
									0);
					mRollerBody.setLinearVelocity(0, 0);
				}
				// kolizja pilki z dolem
				if (ground.collidesWith(mRoller)) {
					updateScore(OPPONENT_ID);
					mRollerBody
							.setTransform(
									(CAMERA_WIDTH / 2)
											/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
									(CAMERA_HEIGHT / 2)
											/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
									0);
					mRollerBody.setLinearVelocity(0, 0);
				}

				if (mPowerup != null
						&& (mPowerup.collidesWith(mPlayerMushroom) || mPowerup
								.collidesWith(mOpponentMushroom))) {
					usePowerup();
				} else {

				}
			}
		});
	}

	private void updateScore(final int pPlayerID) {
		this.scores.increaseScore(pPlayerID);
		mScoreCounter++;
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
		this.mScene.attachChild(okno1);
		this.mScene.attachChild(okno2);
		this.mScene.attachChild(this.mWinnerText);
		// palzuje cala scene, mozna ja przywrocic przy pomocy start()
		this.mEngine.stop();
		// konczy gre i wraca do menu dodane tak na zapas gdyby powrot mial byc
		// na innym klawiszu
		// niz domyslny
		// this.finish();
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
			PhysicsFactory.createBoxBody(this.mPhysicsWorld, left_block,
					BodyType.StaticBody, WALL_FIXTURE_DEF);
			PhysicsFactory.createBoxBody(this.mPhysicsWorld, right_block,
					BodyType.StaticBody, WALL_FIXTURE_DEF);
			// kolor tla ustawiony na tych kwadratach
			left_block.setColor(0.5f, 0.5f, 0.5f);
			right_block.setColor(0.5f, 0.5f, 0.5f);
			this.mScene.attachChild(left_block);
			this.mScene.attachChild(right_block);
			break;
		}

	}

	private void createPowerup() {
		Random generator = new Random();
		int positionX = generator.nextInt(CAMERA_WIDTH - 60);
		int positionY = generator.nextInt(CAMERA_HEIGHT - 60);

		// jezeli nikt nie podniosl powerupa to go usuwamy
		if (mPowerup != null) {
			mPowerup.detachSelf();
			mPowerup = null;
			System.gc();
		}
		mPowerup = new Sprite(positionX, positionY, this.mCirclePowerup,
				this.getVertexBufferObjectManager());
		mPowerup.setScale(0.40f);

		this.mScene.attachChild(mPowerup);
	}

	private void usePowerup() {
		this.mPowerupSound.play();
		mPowerup.detachSelf();
		mPowerup = null;
		Random generator = new Random();
		int gracz;
		System.gc();
		switch (generator.nextInt(8) + 1) {
		case 1:
			Multitouch.this.toast("Punkty dla graczy!");
			// dodaje kazdemu po 1 ptk
			updateScore(PLAYER_ID);
			updateScore(OPPONENT_ID);
			break;
		case 2:
			// dodaje losowemu graczowi 1 ptk
			// losuje komu doda punkt
			gracz = generator.nextInt(2) + 1;
			Multitouch.this.toast("Punkt dla gracza " + gracz + "!");
			updateScore(gracz);
			break;
		case 3:
			Multitouch.this.toast("Zwiêkszenie bramki!");
			// znika sciana przy bramce
			// losuje ktora sciana przy bramce zniknie
			PhysicsConnector physicsConnector;
			gracz = generator.nextInt(2) + 1;
			if (gracz == 1) {
				// wyszukuje polaczenie obiektu z cialem fizycznym po czym
				// wszystko usuwa
				physicsConnector = this.mPhysicsWorld
						.getPhysicsConnectorManager()
						.findPhysicsConnectorByShape(left_ground);
				this.mPhysicsWorld.unregisterPhysicsConnector(physicsConnector);
				this.mPhysicsWorld.destroyBody(left_gBody);
				this.mScene.detachChild(left_ground);

				physicsConnector = this.mPhysicsWorld
						.getPhysicsConnectorManager()
						.findPhysicsConnectorByShape(right_ground);
				this.mPhysicsWorld.unregisterPhysicsConnector(physicsConnector);
				this.mPhysicsWorld.destroyBody(right_gBody);
				this.mScene.detachChild(right_ground);
				// garbage collector
				System.gc();
				// ustawia timer po jakim czasie ma naprawic to co powerup
				// popsul: 1sza wartosc to id efektu, a druga to czas trwania
				powerupEffectsTimeHandler(1, 5);
			} else {
				physicsConnector = this.mPhysicsWorld
						.getPhysicsConnectorManager()
						.findPhysicsConnectorByShape(left_roof);
				this.mPhysicsWorld.unregisterPhysicsConnector(physicsConnector);
				this.mPhysicsWorld.destroyBody(left_rBody);
				this.mScene.detachChild(left_roof);

				physicsConnector = this.mPhysicsWorld
						.getPhysicsConnectorManager()
						.findPhysicsConnectorByShape(right_roof);
				this.mPhysicsWorld.unregisterPhysicsConnector(physicsConnector);
				this.mPhysicsWorld.destroyBody(right_rBody);
				this.mScene.detachChild(right_roof);
				System.gc();
				powerupEffectsTimeHandler(2, 5);
			}
			break;
		case 4:
			Multitouch.this.toast("Blokada bramki!");
			// pojawia sie sciana blokujaca mozliwosc strzelenia gola
			gracz = generator.nextInt(2) + 1;
			if (gracz == 1) {
				blocker_ground = new Rectangle(0, CAMERA_HEIGHT - 2,
						CAMERA_WIDTH, 2, getVertexBufferObjectManager());
				blocker_ground.setColor(0.5f, 0.5f, 0.5f);
				blocker_gBody = PhysicsFactory.createBoxBody(mPhysicsWorld,
						blocker_ground, BodyType.StaticBody, WALL_FIXTURE_DEF);
				mScene.attachChild(blocker_ground);
				powerupEffectsTimeHandler(3, 3);
			} else {
				blocker_roof = new Rectangle(0, 0, CAMERA_WIDTH, 2,
						getVertexBufferObjectManager());
				blocker_ground.setColor(0.5f, 0.5f, 0.5f);
				blocker_rBody = PhysicsFactory.createBoxBody(mPhysicsWorld,
						blocker_roof, BodyType.StaticBody, WALL_FIXTURE_DEF);
				mScene.attachChild(blocker_roof);
				powerupEffectsTimeHandler(4, 3);
			}
			break;
		case 5:
			Multitouch.this.toast("Zmniejszenie pi³ki!");
			// zmniejszenie pilki
			// ustalamy srodek skali bo inaczej sie do srodka mapy teleportuje
			mRoller.setScaleCenter(mRoller.getWidth() / 2,
					mRoller.getHeight() / 2);
			// zmieniamy skale
			mRoller.setScale(0.35f);

			// pobieramy wszystkie fixture po czym zmieniamy ich ksztalt na taki
			// sami jaki ma obiekt
			ArrayList<Fixture> fixtureList1 = mRollerBody.getFixtureList();
			fixtureList1
					.get(0)
					.getShape()
					.setRadius(
							(mRoller.getWidthScaled() / 2)
									/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
			powerupEffectsTimeHandler(5, 10);
			break;
		case 6:
			Multitouch.this.toast("Zwiêkszenie pi³ki!");
			// zwiekszenie pilki
			// ustalamy srodek skali bo inaczej sie do srodka mapy teleportuje
			mRoller.setScaleCenter(mRoller.getWidth() / 2,
					mRoller.getHeight() / 2);
			// zmieniamy skale
			mRoller.setScale(0.75f);
			// pobieramy wszystkie fixture po czym zmieniamy ich ksztalt na taki
			// sami jaki ma obiekt
			ArrayList<Fixture> fixtureList2 = mRollerBody.getFixtureList();
			fixtureList2
					.get(0)
					.getShape()
					.setRadius(
							(mRoller.getWidthScaled() / 2)
									/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
			powerupEffectsTimeHandler(5, 10);
			break;
		default:
			Multitouch.this.toast("Nic!");
		}
	}

	// odlicza czas do pojawienia sie powerupow
	private void createPowerupsTimeHandler() {
		// bedzie sie powtarzac co 10 sekund, zmienic czas mozna podmieniajac 10
		// w linijce nizej
		this.getEngine().registerUpdateHandler(
				powerupTimerHandler = new TimerHandler(10,
						new ITimerCallback() {
							@Override
							public void onTimePassed(
									final TimerHandler pTimerHandler) {
								// tworzymy powerupa
								createPowerup();
								// resetujemy licznik aby dzialalo w kolko
								powerupTimerHandler.reset();
							}
						}));
	}

	// odlicza czas do tych powerupow ktore sa tymczasowe
	private void powerupEffectsTimeHandler(final int nrEfektu, int czasTrwania) {

		// bedzie sie powtarzac co ilosc czasu okreslona w czasTrwania
		this.getEngine().registerUpdateHandler(
				new TimerHandler(czasTrwania,
						new ITimerCallback() {
							@Override
							public void onTimePassed(
									final TimerHandler pTimerHandler) {
								PhysicsConnector physicsConnector;
								switch (nrEfektu) {
								// przywrocenie gornej bramki
								case 1:
									left_ground = new Rectangle(0,
											CAMERA_HEIGHT - 2,
											(float) (CAMERA_WIDTH * 0.2), 2,
											getVertexBufferObjectManager());
									left_ground.setColor(0.5f, 0.5f, 0.5f);
									right_ground = new Rectangle(
											(float) (CAMERA_WIDTH * 0.8),
											CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2,
											getVertexBufferObjectManager());
									right_ground.setColor(0.5f, 0.5f, 0.5f);
									left_gBody = PhysicsFactory.createBoxBody(
											mPhysicsWorld, left_ground,
											BodyType.StaticBody,
											WALL_FIXTURE_DEF);
									right_gBody = PhysicsFactory.createBoxBody(
											mPhysicsWorld, right_ground,
											BodyType.StaticBody,
											WALL_FIXTURE_DEF);
									mScene.attachChild(left_ground);
									mScene.attachChild(right_ground);
									break;
								// przywrocenie dolnej bramki
								case 2:
									left_roof = new Rectangle(0, 0,
											(float) (CAMERA_WIDTH * 0.2), 2,
											getVertexBufferObjectManager());
									left_roof.setColor(0.5f, 0.5f, 0.5f);
									right_roof = new Rectangle(
											(float) (CAMERA_WIDTH * 0.8), 0,
											CAMERA_WIDTH, 2,
											getVertexBufferObjectManager());
									right_roof.setColor(0.5f, 0.5f, 0.5f);
									left_rBody = PhysicsFactory.createBoxBody(
											mPhysicsWorld, left_roof,
											BodyType.StaticBody,
											WALL_FIXTURE_DEF);
									right_rBody = PhysicsFactory.createBoxBody(
											mPhysicsWorld, right_roof,
											BodyType.StaticBody,
											WALL_FIXTURE_DEF);
									mScene.attachChild(left_roof);
									mScene.attachChild(right_roof);
									break;
								// skasowanie gornej blokady
								case 3:
									// wyszukuje polaczenie obiektu z cialem
									// fizycznym po czym wszystko usuwa
									physicsConnector = mPhysicsWorld
											.getPhysicsConnectorManager()
											.findPhysicsConnectorByShape(
													blocker_ground);
									mPhysicsWorld
											.unregisterPhysicsConnector(physicsConnector);
									mPhysicsWorld.destroyBody(blocker_gBody);
									mScene.detachChild(blocker_ground);
									// garbage collector
									System.gc();
									break;
								// skasowanie dolnej blokady
								case 4:
									// wyszukuje polaczenie obiektu z cialem
									// fizycznym po czym wszystko usuwa
									physicsConnector = mPhysicsWorld
											.getPhysicsConnectorManager()
											.findPhysicsConnectorByShape(
													blocker_roof);
									mPhysicsWorld
											.unregisterPhysicsConnector(physicsConnector);
									mPhysicsWorld.destroyBody(blocker_rBody);
									mScene.detachChild(blocker_roof);
									// garbage collector
									System.gc();
									break;
								// przywrocenie pilki do normalnosci
								case 5:
									// ustalamy srodek skali bo inaczej sie do
									// srodka mapy teleportuje
									mRoller.setScaleCenter(
											mRoller.getWidth() / 2,
											mRoller.getHeight() / 2);
									// zmieniamy skale
									mRoller.setScale(0.5f);

									// pobieramy wszystkie fixture po czym
									// zmieniamy ich ksztalt na taki sami jaki
									// ma obiekt
									ArrayList<Fixture> fixtureList1 = mRollerBody
											.getFixtureList();
									fixtureList1
											.get(0)
											.getShape()
											.setRadius(
													(mRoller.getWidthScaled() / 2)
															/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
									break;
								default:
									break;
								}
							}

						}));
	}

}
