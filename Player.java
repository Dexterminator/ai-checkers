import org.omg.CORBA.CODESET_INCOMPATIBLE;

import java.util.*;

public class Player {
    /**
     * Performs a move
     *
     * @param pState
     *            the current state of the board
     * @param pDue
     *            time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play(final GameState pState, final Deadline pDue) {

        Vector<GameState> lNextStates = new Vector<GameState>();
        pState.findPossibleMoves(lNextStates);

        if (lNextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(pState, new Move());
        }

        /**
         * Here you should write your algorithms to get the best next move, i.e.
         * the best next state. This skeleton returns a random move instead.
         */
        int bestValue = Integer.MIN_VALUE;
        GameState bestState = lNextStates.get(0);
        for (GameState state : lNextStates) {
            int value = miniMax(state, 10, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
            if (value > bestValue) {
                bestValue = value;
                bestState = state;
            }
        }
        return bestState;
    }

    private int heuristicValue(GameState state) {
        if (state.isWhiteWin()) {
            System.err.println("White win!");
            return Integer.MAX_VALUE;
        }
        if (state.isRedWin()) {
            System.err.println("Red win!");
            return Integer.MIN_VALUE;
        }
        if (state.isDraw()) {
            System.err.println("Draw!");
            return Integer.MIN_VALUE + 1;
        }

        int redPieces = numberOfType(state, Constants.CELL_RED);
        int whitePieces = numberOfType(state, Constants.CELL_WHITE);
        int redKings = numberOfType(state, Constants.CELL_RED & Constants.CELL_KING);
        int whiteKings = numberOfType(state, Constants.CELL_WHITE & Constants.CELL_KING);

//        System.err.println("Number of white pieces: " + whitePieces);
//        System.err.println("Number of red pieces: " + redPieces);
//        System.err.println("Number of white kings: " + whiteKings);

        int redValue = redPieces + redKings;
        int whiteValue = whitePieces + whiteKings;

        return whiteValue - redValue;
    }

    private int miniMax (GameState node, int depth, int alpha, int beta, boolean maximizing) {
        if (depth == 0 || node.isEOG())
            return heuristicValue(node);
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        if (maximizing) {
            for (GameState child : children) {
                alpha = miniMax(child, depth - 1, alpha, beta, false);
                if (beta <= alpha)
                    break;
            }
            return alpha;
        } else {
            for (GameState child : children) {
                beta = miniMax(child, depth - 1, alpha, beta, true);
                if (beta >= alpha)
                    break;
            }
            return beta;
        }
    }

    private int numberOfType(GameState state, int type) {
        int count = 0;
        for (int i = 1; i <= state.cSquares; i++) {
            int cellValue = state.get(i);
            if ((cellValue & type) != 0)
                count++;
        }
        return count;
    }
}
