package pl.blastteam.iceblast;

import org.andengine.entity.text.Text;

/**
 * @author £ukasz Pogorzelski
 * @author Bart³omiej Tuszkowski
 * @author Tomasz Zaborowski
 * @version 2.0beta
 */
public class Score implements IceBlastConstants {

	private int playerScore;
	private Text playerScoreText;

	private int opponentScore;
	private Text opponentScoreText;

	public Score(Text pPlayerScoreText, Text pOpponentScoreText) {
		playerScore = 0;
		playerScoreText = pPlayerScoreText;
		opponentScore = 0;
		opponentScoreText = pOpponentScoreText;
	}

	/**
	 * Use this constructor only in case of lan multiplayer game and initiate
	 * score text for both players
	 */
	public Score() {
		playerScore = 0;
		opponentScore = 0;
	}

	public void increaseScore(int pPlayerID) {
		if (pPlayerID == PLAYER_ID)
			increasePlayerScore();
		if (pPlayerID == OPPONENT_ID)
			increaseOpponentScore();
	}

	public void increasePlayerScore() {
		playerScore++;
		if (playerScore == 100) {
			playerScore = 0;
		}
		playerScoreText.setText(String.valueOf(playerScore));
	}

	public void increaseOpponentScore() {
		opponentScore++;
		if (opponentScore == 100) {
			opponentScore = 0;
		}
		opponentScoreText.setText(String.valueOf(opponentScore));
	}

	public int getScore(int pPlayerID) {
		if (pPlayerID == PLAYER_ID)
			return getPlayerScore();
		if (pPlayerID == OPPONENT_ID)
			return getOpponentScore();
		return 0;
	}

	public Text getScoreText(int pPlayerID) {
		if (pPlayerID == PLAYER_ID)
			return getPlayerScoreText();
		if (pPlayerID == OPPONENT_ID)
			return getOpponentScoreText();
		return null;
	}

	public int getPlayerScore() {
		return playerScore;
	}

	public Text getPlayerScoreText() {
		return playerScoreText;
	}

	public int getOpponentScore() {
		return opponentScore;
	}

	public Text getOpponentScoreText() {
		return opponentScoreText;
	}

	public void setScoreText(int pPlayerID, Text scoreText) {
		if (pPlayerID == PLAYER_ID)
			setPlayerScoreText(scoreText);
		if (pPlayerID == OPPONENT_ID)
			setOpponentScoreText(scoreText);
	}

	public void setPlayerScoreText(Text playerScoreText) {
		this.playerScoreText = playerScoreText;
	}

	public void setOpponentScoreText(Text opponentScoreText) {
		this.opponentScoreText = opponentScoreText;
	}

	// dla serwera
	public void resetServerScore() {
		this.opponentScore = 0;
		this.playerScore = 0;
	}

	public void increaseServerPlayerScore() {
		playerScore++;
		if (playerScore == 100) {
			playerScore = 0;
		}
	}

	public void increaseServerOpponentScore() {
		opponentScore++;
		if (opponentScore == 100) {
			opponentScore = 0;
		}
	}

	// dla klienta
	public void setClientScore(int pPlayerID, int number) {
		if (pPlayerID == PLAYER_ID) {
			playerScore = number;
			playerScoreText.setText(String.valueOf(playerScore));
		}
		if (pPlayerID == OPPONENT_ID) {
			opponentScore = number;
			opponentScoreText.setText(String.valueOf(opponentScore));
		}
	}

	// zwraca laczna ilosc bramek
	public int getScoreCount() {
		return (playerScore + opponentScore);
	}

}
