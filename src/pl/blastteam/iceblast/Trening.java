package pl.blastteam.iceblast;

import java.io.IOException;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.shape.IAreaShape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
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
import org.andengine.util.debug.Debug;

import android.graphics.Color;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

/**
 * @author £ukasz Pogorzelski
 * @author Bart≥omiej Tuszkowski
 * @author Tomasz Zaborowski
 * @version 2.0
 */
public class Trening extends SimpleBaseGameActivity implements
		IOnSceneTouchListener, IOnAreaTouchListener, IceBlastConstants,
		ContactListener {
	private Score scores;

	// dzwieki
	private Sound mRollerSound;

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private BitmapTextureAtlas mBitmapTextureAtlasBgd;
	private BitmapTextureAtlas mBitmapTextureAtlasTiled;

	private ITextureRegion mBackgroundTextureRegion;
	private ITextureRegion mCircleFaceTextureRegion;
	private ITextureRegion mCirclePlayingBall;

	private Sprite mRoller;
	private Body mRollerBody;
	private Sprite mMushroom;
	private Body mMushromBody;

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

	private MouseJoint mMouseJointActive;
	private Body mGroundBody;

	@Override
	public EngineOptions onCreateEngineOptions() {
		Toast.makeText(this,
				"Sprawdü! Przed graniem z prawdziwym przeciwnikiem!",
				Toast.LENGTH_LONG).show();
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		engineOptions.getAudioOptions().setNeedsSound(true);

		return engineOptions;
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlasBgd = new BitmapTextureAtlas(
				this.getTextureManager(), 480, 800, TextureOptions.BILINEAR);
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(
				this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.mCirclePlayingBall = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"playing_ball_64x64.png", 0, 0);
		// wersja zoptymalizowana z tego co widze textureregion dziala tak samo
		// jak megatekstury aka atlas to 1 obrazek i na nim w pozycjach x i y
		// umieszcza sie nasze mniejsze tekstury
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"mallet_1_64x64.png", 64, 0);
		this.mBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlasBgd, this,
						"game_background_1.png", 0, 0);

		this.mBitmapTextureAtlas.load();
		this.mBitmapTextureAtlasBgd.load();

		this.mBitmapTextureAtlasTiled = new BitmapTextureAtlas(
				this.getTextureManager(), 64, 64, TextureOptions.BILINEAR);
		this.mBitmapTextureAtlasTiled.load();
		// loading font for scoring
		this.SCORE_FONT_SIZE = 32;
		final ITexture scoreFontTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		FontFactory.setAssetBasePath("font/");
		this.mScoreFont = FontFactory.createFromAsset(this.getFontManager(),
				scoreFontTexture, this.getAssets(), "Plok.ttf",
				SCORE_FONT_SIZE, true, Color.WHITE);
		this.mScoreFont.load();

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
		this.mScene.setOnSceneTouchListener(this);
		this.mScene.setOnAreaTouchListener(this);

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		this.mGroundBody = this.mPhysicsWorld.createBody(new BodyDef());

		final VertexBufferObjectManager vertexBufferObjectManager = this
				.getVertexBufferObjectManager();

		// podloga
		final Sprite bgd = new Sprite(0, 0, this.mBackgroundTextureRegion,
				this.getVertexBufferObjectManager());
		bgd.setHeight(CAMERA_HEIGHT);
		bgd.setWidth(CAMERA_WIDTH);
		this.mScene.attachChild(bgd);

		final Rectangle left_ground = new Rectangle(0, CAMERA_HEIGHT - 2,
				(float) (CAMERA_WIDTH * 0.3), 2, vertexBufferObjectManager);
		left_ground.setColor(0.5f, 0.5f, 0.5f);
		final Rectangle right_ground = new Rectangle(
				(float) (CAMERA_WIDTH * 0.7), CAMERA_HEIGHT - 2, CAMERA_WIDTH,
				2, vertexBufferObjectManager);
		right_ground.setColor(0.5f, 0.5f, 0.5f);
		final Rectangle left_roof = new Rectangle(0, 0,
				(float) (CAMERA_WIDTH * 0.3), 2, vertexBufferObjectManager);
		left_roof.setColor(0.5f, 0.5f, 0.5f);
		final Rectangle right_roof = new Rectangle(
				(float) (CAMERA_WIDTH * 0.7), 0, CAMERA_WIDTH, 2,
				vertexBufferObjectManager);
		right_roof.setColor(0.5f, 0.5f, 0.5f);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT,
				vertexBufferObjectManager);
		left.setColor(0.5f, 0.5f, 0.5f);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2,
				CAMERA_HEIGHT, vertexBufferObjectManager);
		right.setColor(0.5f, 0.5f, 0.5f);

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
		this.mScene.attachChild(middleLine);
		// scoring
		final Text opponentScore = new Text(CAMERA_WIDTH / 2 - SCORE_FONT_SIZE
				/ 2, (float) (CAMERA_HEIGHT / 2 - CAMERA_HEIGHT * 0.2)
				- SCORE_FONT_SIZE / 2 + 30, this.mScoreFont, "0", 2,
				vertexBufferObjectManager);
		final Text myScore = new Text(CAMERA_WIDTH / 2 - SCORE_FONT_SIZE / 2,
				(float) (CAMERA_HEIGHT / 2 + CAMERA_HEIGHT * 0.2)
						- SCORE_FONT_SIZE / 2 - 30, this.mScoreFont, "0", 2,
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

		this.addMushroom(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 3 * 2);
		this.addRoller(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);

		// to co jest pomiedzy dziurkami pozniej mozna przesunac poza mape aby
		// tam ona wpadala jak dobry transform bedzie zrobiony bo narazie jest
		// niedobrze :p
		final Rectangle goal1 = new Rectangle((float) (CAMERA_WIDTH * 0.3), 0,
				(float) (CAMERA_WIDTH * 0.4), 0,
				this.getVertexBufferObjectManager());
		final Rectangle goal2 = new Rectangle((float) (CAMERA_WIDTH * 0.3),
				CAMERA_HEIGHT, (float) (CAMERA_WIDTH * 0.4), 0,
				this.getVertexBufferObjectManager());
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, goal1,
				BodyType.StaticBody, GOAL_FIXTURE_DEF);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, goal2,
				BodyType.StaticBody, GOAL_FIXTURE_DEF);
		// kolor tla ustawiony na tych kwadratach
		goal1.setColor(0.09804f, 0.6274f, 0.8784f);
		goal2.setColor(0.09804f, 0.6274f, 0.8784f);
		this.mScene.attachChild(goal1);
		this.mScene.attachChild(goal2);

		// to co jest pomiedzy dziurkami pozniej mozna przesunac poza mape aby
		// tam ona wpadala jak dobry transform bedzie zrobiony bo narazie jest
		// niedobrze :p
		final Rectangle roof = new Rectangle((float) (CAMERA_WIDTH * 0.3),
				0 - 45, (float) (CAMERA_WIDTH * 0.4), 0,
				this.getVertexBufferObjectManager());
		final Rectangle ground = new Rectangle((float) (CAMERA_WIDTH * 0.3),
				CAMERA_HEIGHT + 45, (float) (CAMERA_WIDTH * 0.4), 0,
				this.getVertexBufferObjectManager());
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
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene,
			final TouchEvent pSceneTouchEvent) {
		if (this.mPhysicsWorld != null) {
			switch (pSceneTouchEvent.getAction()) {
			case TouchEvent.ACTION_DOWN:
				return true;
			case TouchEvent.ACTION_MOVE:
				if (this.mMouseJointActive != null) {
					final Vector2 vec = Vector2Pool
							.obtain(pSceneTouchEvent.getX()
									/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
									pSceneTouchEvent.getY()
											/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
					this.mMouseJointActive.setTarget(vec);
					Vector2Pool.recycle(vec);
				}
				return true;
			case TouchEvent.ACTION_UP:
				if (this.mMouseJointActive != null) {
					// ponizsza komenda zatrzymuje cycka kiedy zdejmiemy z niego
					// palec (nie ucieka :p)
					this.mMouseJointActive.getBodyB().setLinearVelocity(0, 0);
					this.mPhysicsWorld.destroyJoint(this.mMouseJointActive);
					this.mMouseJointActive = null;
				}
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
			final ITouchArea pTouchArea, final float pTouchAreaLocalX,
			final float pTouchAreaLocalY) {
		if (pSceneTouchEvent.isActionDown()) {
			final IAreaShape face = (IAreaShape) pTouchArea;
			if (this.mMouseJointActive == null) {
				this.mMouseJointActive = this.createMouseJoint(face,
						pTouchAreaLocalX, pTouchAreaLocalY);
			}
			return true;
		}
		return false;
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();

	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

	}

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

	private void addMushroom(final float pX, final float pY) {
		mMushroom = new Sprite(pX, pY, this.mCircleFaceTextureRegion,
				this.getVertexBufferObjectManager());
		mMushroom.setScale(0.70f);
		mMushromBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld,
				mMushroom, BodyType.DynamicBody, SHROOM_FIXTURE_DEF);

		mMushroom.setUserData(mMushromBody);

		this.mScene.registerTouchArea(mMushroom);
		this.mScene.attachChild(mMushroom);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				mMushroom, mMushromBody, true, true));
	}

	private void addRoller(final float pX, final float pY) {
		mRoller = new Sprite(pX, pY, this.mCirclePlayingBall,
				this.getVertexBufferObjectManager());
		mRoller.setScale(0.60f);
		mRollerBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld,
				mRoller, BodyType.DynamicBody, BALL_FIXTURE_DEF);
		this.mScene.attachChild(mRoller);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				mRoller, mRollerBody, true, true));
	}

	private void addObstacle(final float pX, final float pY) {
		final Rectangle left_block = new Rectangle(pX, pY, 30, 30,
				this.getVertexBufferObjectManager());
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left_block,
				BodyType.StaticBody, WALL_FIXTURE_DEF);
		// kolor tla ustawiony na tych kwadratach
		left_block.setColor(0.5f, 0.5f, 0.5f);
		this.mScene.attachChild(left_block);
	}

	private void registerUpdateHandler(final Rectangle roof,
			final Rectangle ground) {
		this.mScene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() {
			}

			@Override
			public void onUpdate(final float pSecondsElapsed) {
				// kolizja pilki z gora (chyba)
				if (roof.collidesWith(mRoller)) {
					// zwiekszenie punktacji
					updateScore(PLAYER_ID);
					// cialo przenosi sie do pozycji 50x50 potem sie poprawi
					mRollerBody
							.setTransform(
									(CAMERA_WIDTH / 2)
											/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
									(CAMERA_HEIGHT / 2)
											/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
									0);
					mRollerBody.setLinearVelocity(0, 0);
				}
				// kolizja pilki z dolem (chyba)
				if (ground.collidesWith(mRoller)) {
					updateScore(OPPONENT_ID);
					// cialo przenosi sie do pozycji 50x50 potem sie poprawi
					mRollerBody
							.setTransform(
									(CAMERA_WIDTH / 2)
											/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
									(CAMERA_HEIGHT / 2)
											/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
									0);
					mRollerBody.setLinearVelocity(0, 0);
				}
			}
		});
	}

	private void updateScore(final int pPlayerID) {
		this.scores.increaseScore(pPlayerID);
		if (this.scores.getScore(PLAYER_ID) == 5) {
			this.addObstacle((CAMERA_WIDTH / 2 - 15), (CAMERA_HEIGHT / 2 / 3));
		}
		if (this.scores.getScore(pPlayerID) == 10) {
			Text text = this.scores.getScoreText(pPlayerID);
			text.setPosition(text.getX() - SCORE_FONT_SIZE / 2, text.getY());
		}
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
}
