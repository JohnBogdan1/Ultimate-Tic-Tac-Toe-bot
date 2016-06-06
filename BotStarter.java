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
 * BotStarter class
 * 
 * Magic happens here. You should edit this file, or more specifically the
 * makeTurn() method to make your bot do more than random moves.
 * 
 * @author Jim van Eeden <jim@starapple.nl>
 */

public class BotStarter {
	private int lx, hx, ly, hy;
	private final int maxScore = 123456789;
	private final int minScore = -123456789;

	private int winMicroScore = 7;
	private int MacroBoardWeight = 23;

	// declar o matrice cu 3 dimensiuni, in care retin secventele posibile castigatoare
	private int[][][] posibleWinSeq = new int[][][] { { { 0, 0 }, { 0, 1 }, { 0, 2 } },
			{ { 1, 0 }, { 1, 1 }, { 1, 2 } }, { { 2, 0 }, { 2, 1 }, { 2, 2 } }, { { 0, 0 }, { 1, 0 }, { 2, 0 } },
			{ { 0, 1 }, { 1, 1 }, { 2, 1 } }, { { 0, 2 }, { 1, 2 }, { 2, 2 } }, { { 0, 0 }, { 1, 1 }, { 2, 2 } },
			{ { 0, 2 }, { 1, 1 }, { 2, 0 } } };

	// limitele(lowX, lowY) din mBoard pentru fiecare patrat
	private int[][] miniBoardsBounds = new int[][] { { 0, 0 }, { 3, 0 }, { 6, 0 }, { 0, 3 }, { 3, 3 }, { 6, 3 },
			{ 0, 6 }, { 3, 6 }, { 6, 6 } };

	// cat de importante sunt acele patrate(blocuri)
	private int[] weights = new int[] { 3, 2, 3, 2, 4, 2, 3, 2, 3 };
	private int[][] matrixWeights = new int[][] { {3, 2, 3}, {2, 4, 2}, {3, 2, 3 } };

	/**
	 * Functie de evaluare a mutarii.
	 * @return scorul evaluarii.
	 */
	private int evaluate(Field field, int player) {

		// calculez scorul pentru MacroBoard
		// il inmultesc cu un weight, deoarece este mai important MacroBoard ca un MiniBoard
		int value = calculateMacroBoardScore(field, player) * MacroBoardWeight;

		int lowX, lowY;

		// calculez pentru fiecare patrat scorul
		for (int i = 0; i < 9; i++) {

			lowX = miniBoardsBounds[i][0];
			lowY = miniBoardsBounds[i][1];

			// daca nu este un patrat terminat
			if (field.getmMacroboard()[lowX / 3][lowY / 3] == 0 || field.getmMacroboard()[lowX / 3][lowY / 3] == -1) {

				// la acel scor adun scorul pentru fiecare miniBoard
				// pe care il inmultesc cu valoarea respectiva asociata patratului
				value += calculateBoardScore(field, lowX, lowY, player) * weights[i];
			}
		}

		return value;
	}

	/**
	 * Calculez scorul fiecarui miniBoard.
	 */
	private int calculateBoardScore(Field field, int lowX, int lowY, int player) {

		int playerScore = 0, opponentScore = 0;
		int playerBlocks = 0, opponentBlocks = 0;
		boolean fullSquare;

		// pentru fiecare secventa castigatoare dintr-un patrat
		for (int[][] seq : posibleWinSeq) {
			fullSquare = false;
			playerBlocks = 0;
			opponentBlocks = 0;

			// calculez cate blocuri am eu si cate are oponentul
			for (int[] index : seq) {

				if (field.getmBoard()[lowX + index[0]][lowY + index[1]] == player)
					playerBlocks++;
				else if (field.getmBoard()[lowX + index[0]][lowY + index[1]] == player % 2 + 1)
					opponentBlocks++;
				else if (field.getmBoard()[lowX + index[0]][lowY + index[1]] == -2)
					fullSquare = true;

			}

			// daca o secventa contine un bloc al meu, adun 1
			// daca o secventa contine doua blocuri identice, adun 1 + un scor
			// daca intr-o secventa se gasesc blocuri diferite
			// atunci nu mai poate fi o secventa castigatoare
			if (!fullSquare) {
				if (playerBlocks > 0) {
					if (opponentBlocks > 0)
						continue;
					if (playerBlocks == 2)
						playerScore += winMicroScore;
					playerScore += 1;
				} else if (opponentBlocks > 0) {
					if (opponentBlocks == 2)
						opponentScore += winMicroScore;
					opponentScore += 1;
				}
			}
		}

		int value = 0;
		// de asemenea, calculez cat de "valoros" este patratul
		// facand referire la weights
		int x = 0, y = 0;
		for (int i = lowX; i < lowX + 3; i++) {
			y = 0;
			for (int j = lowY; j < lowY + 3; j++) {
				if (field.getmBoard()[i][j] == player) {
					value += matrixWeights[x][y];
				} else if (field.getmBoard()[i][j] == player % 2 + 1) {
					value -= matrixWeights[x][y];
				}
				y++;
			}
			x++;
		}

		// adun valoarea de mai sus cu diferenta dintre scorul meu si scorul oponentului
		return playerScore - opponentScore + value;
	}
	
	/**
	 * Calculez scorul asociat lui macroBoard. Valoarea returnata va fi inmultita mai sus
	 * cu o alta valoare, deoarece un patrat din macroBoard este mai important ca unul 
	 * din miniBoard.
	 */
	private int calculateMacroBoardScore(Field field, int player) {

		int playerScore = 0, opponentScore = 0;
		int playerBlocks = 0, opponentBlocks = 0;
		boolean fullSquare;

		// pentru fiecare secventa castigatoare din patrat
		for (int[][] seq : posibleWinSeq) {
			fullSquare = false;
			playerBlocks = 0;
			opponentBlocks = 0;

			// calculez cate blocuri am eu si cate are oponentul
			for (int[] index : seq) {

				if (field.getmMacroboard()[index[0]][index[1]] == player)
					playerBlocks++;
				else if (field.getmMacroboard()[index[0]][index[1]] == player % 2 + 1)
					opponentBlocks++;
				else if (field.getmMacroboard()[index[0]][index[1]] == -2)
					fullSquare = true;

			}

			// daca o secventa contine un bloc al meu, adun 1
			// daca o secventa contine doua blocuri identice, adun 1 + un scor
			// daca intr-o secventa se gasesc blocuri diferite
			// atunci nu mai poate fi o secventa castigatoare
			if (!fullSquare) {
				if (playerBlocks > 0) {
					if (opponentBlocks > 0)
						continue;
					if (playerBlocks == 2)
						playerScore += winMicroScore;
					playerScore += 1;
				} else if (opponentBlocks > 0) {
					if (opponentBlocks == 2)
						opponentScore += winMicroScore;
					opponentScore += 1;
				}
			}
		}

		int value = 0;
		
		// de asemenea, calculez "valoarea" patratului
		// am mai mult ca preferinta castigarea colturilor si a centrului 
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (field.getmMacroboard()[i][j] == player) {
					value += matrixWeights[i][j];
				} else if (field.getmMacroboard()[i][j] == player % 2 + 1) {
					value -= matrixWeights[i][j];
				}
			}
		}

		// adun valoarea de mai sus cu diferenta dintre scorul meu si
		// scorul oponentului
		return playerScore - opponentScore + value;
	}

	
	/*
	 * Setez un patrat ca fiind terminat in MacroBoard, in urma unei mutari,
	 * daca este cazul sa fie castigat de oricare din playeri sau terminat.
	 */
	private void setMicroWins(Field field) {

		// verific daca e castigator patratul(sau terminat in urma unui egal)
		int score_id = field.checkForVictory(lx, hx, ly, hy);

		// daca eu castig
		if (score_id == BotParser.mBotId) {

			// cand mutarea trimite catre acelasi patrat care devine castigat
			// atunci toate valorile cu 0 din MacroBoard devin -1
			if (field.getPlayerIdFromMacro(lx / 3, ly / 3) == -1) {
				field.clearMacroBoardWhenSentToAWonSquare();
			}

			field.setWinnerMicroSquare(lx / 3, ly / 3, score_id);
			// daca oponentul castiga
		} else if (score_id == BotParser.mBotId % 2 + 1) {

			if (field.getPlayerIdFromMacro(lx / 3, ly / 3) == -1) {
				field.clearMacroBoardWhenSentToAWonSquare();
			}

			field.setWinnerMicroSquare(lx / 3, ly / 3, score_id);
			// daca este egal
		} else if (score_id == -1) {

			if (field.getPlayerIdFromMacro(lx / 3, ly / 3) == -1) {
				field.clearMacroBoardWhenSentToAWonSquare();
			}

			// -2 sta pentru egal in simularea mea; pe platforma este tot 0
			field.setWinnerMicroSquare(lx / 3, ly / 3, -2);

		}

	}

	private Move minimax(Field field, int player, int depth, int alpha, int beta) {

		// setez un patrat ca fiind terminat in MacroBoard, in urma unei mutari
		// daca este cazul sa fie castigat de oricare din playeri sau terminat
		setMicroWins(field);

		// Verific daca este victorie, infrangere sau egal in MacroBoard
		int macroScore = field.checkmMacroBoardForVictory();

		// daca eu castig
		if (macroScore == BotParser.mBotId) {

			Move move = new Move();
			move.setScore(maxScore + depth);
			return move;

		// daca oponentul castiga
		} else if (macroScore == BotParser.mBotId % 2 + 1) {

			Move move = new Move();
			move.setScore(minScore - depth);
			return move;

		// daca este egal
		} else if (macroScore == -3) {

			Move move = new Move();
			move.setScore(0);
			return move;

		// daca nu este niciuna de mai sus si am ajuns la final
		} else if (depth == 0) {

			if (player == BotParser.mBotId) {
				Move move = new Move();
				move.setScore(evaluate(field, player));
				return move;
			} else if (player == BotParser.mBotId % 2 + 1) {
				Move move = new Move();
				move.setScore(-evaluate(field, player));
				return move;
			}

		}

		Move bestMove = new Move();

		for (Move move : field.getAvailableMoves()) {

			// determin limitele patratului in care se afla mutarea
			boundaries(move);

			// setez mutarea in mBoard
			field.setMove(move.mX, move.mY, player);

			// daca este randul meu
			if (player == BotParser.mBotId) {

				// calculez scorul mutarii, apeland recursiv pe min
				move.score = minimax(field, (player % 2) + 1, depth - 1, alpha, beta).score;

				// daca scorul este mai mare ca alpha, actualizez alpha
				// setez cea mai buna mutare pana acum, bestMove, in cazul lui
				// max
				if (move.score > alpha) {

					alpha = move.score;
					bestMove = move;
				}
			// daca este randul lui
			} else if (player == BotParser.mBotId % 2 + 1) {

				// // calculez scorul mutarii, apeland recursiv pe max
				move.score = minimax(field, (player % 2) + 1, depth - 1, alpha, beta).score;

				// daca scorul este mai mic ca beta, actualizez beta
				// setez cea mai buna mutare pana acum, bestMove, in cazul lui
				// min
				if (move.score < beta) {
					beta = move.score;
					bestMove = move;
				}

			}

			// fac unMove la mutare
			field.unMove(move.mX, move.mY, 0);

			// intrerup cautarea, deoarece nu mai are cum sa influenteze
			// rezultatul
			if (alpha >= beta) {
				break;
			}
		}

		// returnez alpha si mutarea cea mai buna, daca sunt eu
		// respectiv beta si mutarea cea mai buna, daca este oponentul
		if (player == BotParser.mBotId) {
			bestMove.score = alpha;
		} else {
			bestMove.score = beta;
		}

		return bestMove;

	}

	private void checkX(Move move) {
		if (move.mX < 3) {
			lx = 0;
			hx = 3;
		} else if (move.mX >= 3 && move.mX < 6) {
			lx = 3;
			hx = 6;
		} else if (move.mX >= 6 && move.mX < 9) {
			lx = 6;
			hx = 9;
		}
	}

	/**
	 * Determin in care patrat din mBoard se afla mutarea.
	 * 
	 * @param move
	 *            - mutarea respectiva.
	 */
	private void boundaries(Move move) {
		if (move.mY < 3) {
			ly = 0;
			hy = 3;
			checkX(move);
		} else if (move.mY >= 3 && move.mY < 6) {
			ly = 3;
			hy = 6;
			checkX(move);
		} else if (move.mY >= 6 && move.mY < 9) {
			ly = 6;
			hy = 9;
			checkX(move);
		}
	}

	/**
	 * Makes a turn. Edit this method to make your bot smarter. Currently does
	 * only random moves.
	 *
	 * @return The column where the turn was made.
	 */
	public Move makeTurn(Field field) {

		ArrayList<Move> moves = field.getAvailableMoves();
		Move move = new Move();

		// prima mutare
		if (moves.size() == 81) {
			move = new Move(4, 4);

		// primele mutari
		} else if (moves.size() >= 7 && !field.checkMacro()) {
			int depth = 8;
			boundaries(moves.get(0));
			move = minimax(field, BotParser.mBotId, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);

		// cand am putine mutari
		} else if (moves.size() <= 4) {
			int depth = 8;
			boundaries(moves.get(0));
			move = minimax(field, BotParser.mBotId, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);

		// cand am multe mutari(branch factor mare)
		} else if (moves.size() > 25) {
			int depth = 5;
			boundaries(moves.get(0));
			move = minimax(field, BotParser.mBotId, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);

		// cand ma aflu spre mijlocul/finalul meciului
		} else {
			int depth = 7;
			boundaries(moves.get(0));
			move = minimax(field, BotParser.mBotId, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}

		// returnez cea mai buna mutare gasita
		return move;
	}

	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}
}
