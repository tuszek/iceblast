package pl.blastteam.iceblast;

/**
 * @author £ukasz Pogorzelski
 * @author Bart³omiej Tuszkowski
 * @author Tomasz Zaborowski
 * @version 2.0beta
 */
public interface IceBlastConstants {
	
	//ukradzione zmienne bo tak nie pyskuj
	public static final int FPS = 30;
	public static final int BALL_WIDTH = 32;
	public static final int BALL_HEIGHT = 32;
	public static final int BALL_RADIUS = BALL_WIDTH / 2;
	public static final int GAME_WIDTH = 720;
	public static final int GAME_WIDTH_HALF = GAME_WIDTH / 2;
	public static final int GAME_HEIGHT = 480;
	public static final int GAME_HEIGHT_HALF = GAME_HEIGHT / 2;
	public static final float PADDLE_WIDTH = 64 * 0.7f;
	public static final float PADDLE_WIDTH_HALF = PADDLE_WIDTH / 2;
	public static final float PADDLE_HEIGHT = 64 * 0.7f;
	public static final float PADDLE_HEIGHT_HALF = PADDLE_HEIGHT / 2;
	
	public static final int PLAYER_ID = 1;
	public static final int OPPONENT_ID = PLAYER_ID + 1;
	public static final int USER_NOT_SET = OPPONENT_ID + 1;
	
	public static final int GAME_SPACE_WIDTH = 64;
	public static final int CAMERA_WIDTH = GAME_SPACE_WIDTH * 3;
	public static final int CAMERA_HEIGHT = GAME_SPACE_WIDTH * 5;

	// Multiplayer constants
	public static final String LOCALHOST_IP = "127.0.0.1";
	public static final int SERVER_PORT = 4444;
	/* Server --> Client */
	public static final short FLAG_MESSAGE_SERVER_SET_MUSHROOMID = 1;
	public static final short FLAG_MESSAGE_SERVER_UPDATE_SCORE = FLAG_MESSAGE_SERVER_SET_MUSHROOMID + 1;
	public static final short FLAG_MESSAGE_SERVER_UPDATE_BALL = FLAG_MESSAGE_SERVER_UPDATE_SCORE + 1;
	public static final short FLAG_MESSAGE_SERVER_UPDATE_MUSHROOM = FLAG_MESSAGE_SERVER_UPDATE_BALL + 1;

	/* Client --> Server */
	public static final short FLAG_MESSAGE_CLIENT_MOVE_MUSHROOM = 1;	
	
	public static final int DIALOG_CHOOSE_SERVER_OR_CLIENT_ID = 0;
	public static final int DIALOG_ENTER_SERVER_IP_ID = DIALOG_CHOOSE_SERVER_OR_CLIENT_ID + 1;
	public static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_ENTER_SERVER_IP_ID + 1;
	
	// tag for debug purpose
	public static final String TAG = "ICE_BLAST";
	
	/* The categories. */
	public static final short CATEGORYBIT_GOAL = 1;
	public static final short CATEGORYBIT_SHROOM = 2;
	public static final short CATEGORYBIT_BALL = 4;
	public static final short CATEGORYBIT_WALL = 8;
	
	/* And what should collide with what. */
	public static final short MASKBITS_GOAL = CATEGORYBIT_WALL + CATEGORYBIT_GOAL + CATEGORYBIT_SHROOM; //blokuje sciane + bramke + grzybka
	public static final short MASKBITS_SHROOM = CATEGORYBIT_WALL + CATEGORYBIT_SHROOM + CATEGORYBIT_GOAL + CATEGORYBIT_BALL; //blokuje sciane + grzybka, bramke i pilke
	public static final short MASKBITS_BALL = CATEGORYBIT_WALL + CATEGORYBIT_BALL + CATEGORYBIT_SHROOM; //blokuje sciane + pilke i grzybka
	public static final short MASKBITS_WALL = CATEGORYBIT_WALL + CATEGORYBIT_SHROOM + CATEGORYBIT_GOAL + CATEGORYBIT_BALL; //blokuje sciane + grzybka, bramke i pilke
	
	//bity dla gornej i dolnej bramki aby je rozroznic przy detekcji
	public static final short CATEGORYBIT_HIGH_DETECTOR = 16;
	public static final short CATEGORYBIT_LOW_DETECTOR = 32;
	/* And what should collide with what. */
	public static final short MASKBITS_LAN_GOAL = CATEGORYBIT_WALL + CATEGORYBIT_GOAL + CATEGORYBIT_SHROOM; //blokuje sciane + bramke + grzybka
	public static final short MASKBITS_LAN_SHROOM = CATEGORYBIT_WALL + CATEGORYBIT_SHROOM + CATEGORYBIT_GOAL + CATEGORYBIT_BALL; //blokuje sciane + grzybka, bramke i pilke
	public static final short MASKBITS_LAN_BALL = CATEGORYBIT_WALL + CATEGORYBIT_BALL + CATEGORYBIT_SHROOM + CATEGORYBIT_HIGH_DETECTOR + CATEGORYBIT_LOW_DETECTOR; //blokuje sciane + pilke i grzybka
	public static final short MASKBITS_LAN_WALL = CATEGORYBIT_WALL + CATEGORYBIT_SHROOM + CATEGORYBIT_GOAL + CATEGORYBIT_BALL; //blokuje sciane + grzybka, bramke i pilke
	//ogolnie toto moglo puste pozostac bo z niczym nie ma kolidowac, no ale tak just in case dodalem blokade na martwe obiekty
	public static final short MASKBITS_LAN_HIGH_DETECTOR = CATEGORYBIT_HIGH_DETECTOR + CATEGORYBIT_LOW_DETECTOR + CATEGORYBIT_BALL; //blokuje sciane + obydwa detektort
	public static final short MASKBITS_LAN_LOW_DETECTOR = CATEGORYBIT_HIGH_DETECTOR + CATEGORYBIT_LOW_DETECTOR + CATEGORYBIT_BALL; //blokuje sciane + obydwa detektory
	
}
