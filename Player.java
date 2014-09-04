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
         * Select best next state based on the minimax algorithm
         */
        int bestValue = Integer.MIN_VALUE;
        GameState bestState = lNextStates.get(0);
        for (GameState state : lNextStates) {
            int value = miniMax(state, 10, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            if (value > bestValue) {
                bestValue = value;
                bestState = state;
                break;
            }
        }

//        System.err.println("Best value: " + bestValue);
//        System.err.println("Minimax from root value: " + miniMax(pState, 10, Integer.MIN_VALUE, Integer.MAX_VALUE, true));
//        System.err.println("Number of white pawns: " + numberOfType(bestState, Constants.CELL_WHITE));
//        System.err.println("Number of white kings: " + numberOfType(bestState, Constants.CELL_WHITE | Constants.CELL_KING));
        miniMax(pState, 10, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        return bestState;
    }

    private int heuristicValue(GameState state) {
        if (state.isWhiteWin()) {
            return Integer.MAX_VALUE;
        }
        if (state.isRedWin()) {
            return Integer.MIN_VALUE;
        }
        if (state.isDraw()) {
            return Integer.MIN_VALUE + 1;
        }

        int redPawns = numberOfType(state, Constants.CELL_RED);
        int whitePawns = numberOfType(state, Constants.CELL_WHITE);
        int redKings = numberOfType(state, Constants.CELL_RED | Constants.CELL_KING);
        int whiteKings = numberOfType(state, Constants.CELL_WHITE | Constants.CELL_KING);

        int redValue = redPawns + redKings * 2;
        int whiteValue = whitePawns + whiteKings * 2;

        return whiteValue - redValue;
    }

    private int miniMax (GameState node, int depth, int alpha, int beta, boolean maximizing) {
        if (depth == 0 || node.isEOG())
            return heuristicValue(node);
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        if (maximizing) {
            for (GameState child : children) {
                alpha = Math.max(alpha, miniMax(child, depth - 1, alpha, beta, false));
                if (beta <= alpha)
                    break;
            }
            return alpha;
        } else {
            for (GameState child : children) {
                beta = Math.min(beta, miniMax(child, depth - 1, alpha, beta, true));
                if (beta <= alpha)
                    break;
            }
            return beta;
        }
    }

    private int numberOfType(GameState state, int type) {
        int count = 0;
        for (int i = 1; i <= state.cSquares; i++) {
            int cellValue = state.get(i);
            if (cellValue == type)
                count++;
        }
        return count;
    }
}
