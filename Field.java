// // Copyright 2016 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//  
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package bot;

import java.util.ArrayList;

/**
 * Field class
 * 
 * Handles everything that has to do with the field, such as storing the current
 * state and performing calculations on the field.
 * 
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 */

public class Field {
	@SuppressWarnings("unused")
	private int mRoundNr;
	private int mMoveNr;
	private int[][] mBoard;
	private int[][] mMacroboard;

	private final int COLS = 9, ROWS = 9;
	private String mLastError = "";

	public Field() {
		mBoard = new int[COLS][ROWS];
		mMacroboard = new int[COLS / 3][ROWS / 3];
		clearBoard();
	}

	/**
	 * Parse data about the game given by the engine
	 * 
	 * @param key
	 *            : type of data given
	 * @param value
	 *            : value
	 */
	public void parseGameData(String key, String value) {
		if (key.equals("round")) {
			mRoundNr = Integer.parseInt(value);
		} else if (key.equals("move")) {
			mMoveNr = Integer.parseInt(value);
		} else if (key.equals("field")) {
			parseFromString(value); /* Parse Field with data */
		} else if (key.equals("macroboard")) {
			parseMacroboardFromString(value); /* Parse macroboard with data */
		}
	}

	/**
	 * Initialise field from comma separated String
	 * 
	 * @param String
	 *            :
	 */
	public void parseFromString(String s) {
		System.err.println("Move " + mMoveNr);
		s = s.replace(";", ",");
		String[] r = s.split(",");
		int counter = 0;
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLS; x++) {
				mBoard[x][y] = Integer.parseInt(r[counter]);
				counter++;
			}
		}
	}

	/**
	 * Initialise macroboard from comma separated String
	 * 
	 * @param String
	 *            :
	 */
	public void parseMacroboardFromString(String s) {
		String[] r = s.split(",");
		int counter = 0;
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				mMacroboard[x][y] = Integer.parseInt(r[counter]);
				counter++;
			}
		}
	}

	public void clearBoard() {
		for (int x = 0; x < COLS; x++) {
			for (int y = 0; y < ROWS; y++) {
				mBoard[x][y] = 0;
			}
		}
	}

	public int[][] getmMacroboard() {
		return mMacroboard;
	}

	public int[][] getmBoard() {
		return mBoard;
	}

	/**
	 * Preiau mutarile valabile in acest moment.
	 * 
	 * @return
	 */
	public ArrayList<Move> getAvailableMoves() {
		ArrayList<Move> moves = new ArrayList<Move>();

		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLS; x++) {
				if (isInActiveMicroboard(x, y) && mBoard[x][y] == 0) {
					moves.add(new Move(x, y));
				}
			}
		}

		return moves;
	}

	/**
	 * Verific care este patratul in care trebuie sa pun.
	 */
	public Boolean isInActiveMicroboard(int x, int y) {
		return mMacroboard[(int) x / 3][(int) y / 3] == -1;
	}

	/**
	 * Matrice auxiliara pe care o folosesc pentru a prelua o matrice din
	 * mBoard.
	 * 
	 * @param lx
	 *            - lowX
	 * @param hx
	 *            - highX
	 * @param ly
	 *            - lowY
	 * @param hy
	 *            - highY
	 * @return a 3x3 matrix
	 */
	public int[][] matrix(int lx, int hx, int ly, int hy) {
		int[][] matrix = new int[3][3];
		int i = 0, j = 0;
		for (int x = lx; x < hx; x++) {
			for (int y = ly; y < hy; y++) {
				matrix[i][j] = mBoard[x][y];
				j++;
			}
			i++;
			j = 0;
		}

		return matrix;

	}

	/**
	 * Verifica daca sunt trimis catre un patrat castigat. In acest caz, exista
	 * mai multe valori de -1 in MacroBoard.
	 * 
	 * @return
	 */
	public boolean checkMacro() {
		int count = 0;
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (mMacroboard[x][y] == -1)
					count++;
			}
		}
		if (count > 1)
			return true;
		return false;
	}

	/**
	 * Verific daca este victorie, infrangere sau egal intr-un patrat de 3x3.
	 * Daca nu este nixiuna, returnez 0.
	 * 
	 * @return id-ul playerului castigator, -1 in caz de egal sau 0.
	 */
	public int checkForVictory(int lx, int hx, int ly, int hy) {

		boolean victory = false;
		int id;

		// creez o matrice noua
		// pe care o preiau din Mboard de la acele coordonate.
		int[][] matrix = matrix(lx, hx, ly, hy);

		// Verific liniile din matrice
		for (int x = 0; x < 3; x++) {
			id = matrix[x][0];

			if (id != 0) {
				victory = true;

				for (int y = 0; y < 3; y++) {
					if (matrix[x][y] != id) {
						victory = false;
						break;
					}
				}

				if (victory == true)
					return id;
			}
		}

		// Verific coloanele din matrice
		for (int y = 0; y < 3; y++) {

			id = matrix[0][y];

			if (id != 0) {
				victory = true;
				for (int x = 0; x < 3; x++) {

					if (matrix[x][y] != id) {
						victory = false;
						break;
					}
				}
				if (victory == true)
					return id;
			}
		}

		// Verific diagonala principala din matrice
		id = matrix[0][0];
		if (matrix[1][1] == id && matrix[2][2] == id) {
			return id;
		}

		// Verific diagonala secundara din matrice
		id = matrix[0][2];
		if (matrix[1][1] == id && matrix[2][0] == id) {
			return id;
		}

		// Daca mai exista celule libere, atunci nu este un patrat terminat
		// returnez 0 in acest caz
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (matrix[x][y] == 0)
					return 0;
			}
		}

		// altfel este egalitate
		return -1;
	}

	/**
	 * Returns reason why addMove returns false
	 * 
	 * @param args
	 *            :
	 * @return : reason why addMove returns false
	 */
	public String getLastError() {
		return mLastError;
	}

	/**
	 * Setez castigatorul intr-un patrat din MacroBoard.
	 * 
	 * @param x
	 * @param y
	 * @param winner
	 */
	public void setWinnerMicroSquare(int x, int y, int winner) {
		mMacroboard[x][y] = winner;

	}

	/**
	 * Setez mutarea curenta.
	 */

	public void setMove(int x, int y, int player) {

		mBoard[x][y] = player;

		mMacroboard[x / 3][y / 3] = 0;

		// daca patratul catre care trimite mutarea are valoarea 0, il fac -1
		// daca patratul catre care trimite mutarea e terminat
		// atunci toate patratele cu valoarea 0 devin -1
		if (mMacroboard[x % 3][y % 3] == 0)
			mMacroboard[x % 3][y % 3] = -1;
		else if (mMacroboard[x % 3][y % 3] == 1 || mMacroboard[x % 3][y % 3] == 2 || mMacroboard[x % 3][y % 3] == -2)
			clearMacroBoardWhenSentToAWonSquare();

	}

	/**
	 * Fac unMove la o mutare setata de setMove. Operatiile sunt in oglinda.
	 */
	public void unMove(int x, int y, int player) {
		mBoard[x][y] = player;

		if (mMacroboard[x % 3][y % 3] == -1)
			mMacroboard[x % 3][y % 3] = 0;
		else if (mMacroboard[x % 3][y % 3] == 1 || mMacroboard[x % 3][y % 3] == 2 || mMacroboard[x % 3][y % 3] == -2)
			unClearMacroBoardWhenSentToAWonSquare();

		mMacroboard[x / 3][y / 3] = -1;
	}

	/**
	 * Daca oponentul ma trimite catre un patrat castigat, atunci toate valorile
	 * cu 0 din MacroBoard devin -1. Valabil pentru ambii playeri.
	 */
	public void clearMacroBoardWhenSentToAWonSquare() {
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (mMacroboard[x][y] == 0)
					mMacroboard[x][y] = -1;
			}
		}
	}

	/**
	 * Cand fac unMove in urma unei mutari care a trimis catre un patrat
	 * terminat.
	 */
	public void unClearMacroBoardWhenSentToAWonSquare() {
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (mMacroboard[x][y] == -1)
					mMacroboard[x][y] = 0;
			}
		}
	}

	/**
	 * Verific daca este victorie, infrangere sau egal in MacroBoard. Daca nu
	 * este niuciuna returnez 0.
	 * 
	 * @return id-ul player-ului castigator, -1 in caz de egal sau 0.
	 */
	public int checkmMacroBoardForVictory() {

		boolean victory = false;
		int id;

		// Verific liniile
		for (int x = 0; x < 3; x++) {
			id = mMacroboard[x][0];

			if (id != 0 && id != -1 && id != -2) {
				victory = true;

				for (int y = 0; y < 3; y++) {
					if (mMacroboard[x][y] != id) {
						victory = false;
						break;
					}
				}

				if (victory == true)
					return id;
			}
		}

		// Verific coloanele
		for (int y = 0; y < 3; y++) {
			id = mMacroboard[0][y];

			if (id != 0 && id != -1 && id != -2) {
				victory = true;

				for (int x = 0; x < 3; x++) {
					if (mMacroboard[x][y] != id) {
						victory = false;
						break;
					}
				}
				if (victory == true)
					return id;
			}
		}

		// Verific diagonala principala
		id = mMacroboard[0][0];
		if (mMacroboard[1][1] == id && mMacroboard[2][2] == id) {
			return id;
		}

		// Verific diagonala secundara
		id = mMacroboard[0][2];
		if (mMacroboard[1][1] == id && mMacroboard[2][0] == id) {
			return id;
		}

		// daca mai exista mutari, se mai poate juca
		if (getAvailableMoves().size() > 0) {
			return 0;
		}

		// altfel este egalitate
		return -3;
	}

	@Override
	/**
	 * Creates comma separated String with player ids for the microboards.
	 * 
	 * @param args
	 *            :
	 * @return : String with player names for every cell, or 'empty' when cell
	 *         is empty.
	 */
	public String toString() {
		String r = "";
		int counter = 0;
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLS; x++) {
				if (counter > 0) {
					r += ",";
				}
				r += mBoard[x][y];
				counter++;
			}
		}
		return r;
	}

	/**
	 * Checks whether the field is full
	 * 
	 * @param args
	 *            :
	 * @return : Returns true when field is full, otherwise returns false.
	 */
	public boolean isFull() {
		for (int x = 0; x < COLS; x++)
			for (int y = 0; y < ROWS; y++)
				if (mBoard[x][y] == 0)
					return false; // At least one cell is not filled
		// All cells are filled
		return true;
	}

	public int getNrColumns() {
		return COLS;
	}

	public int getNrRows() {
		return ROWS;
	}

	public boolean isEmpty() {
		for (int x = 0; x < COLS; x++) {
			for (int y = 0; y < ROWS; y++) {
				if (mBoard[x][y] > 0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Preiau MacroBoard.
	 * 
	 * @param column
	 * @param row
	 * @return
	 */
	public int getPlayerIdFromMacro(int column, int row) {
		return mMacroboard[column][row];
	}

	/**
	 * Returneaza id-ul player-ului de la pozitia respectiva.
	 * 
	 * @param args
	 *            : int column, int row
	 * @return : int
	 */
	public int getPlayerId(int column, int row) {
		return mBoard[column][row];
	}
}