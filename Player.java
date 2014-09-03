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
            int value = miniMax(state, 5, true);
            if (value > bestValue) {
                bestValue = value;
                bestState = state;
            }
        }
        return bestState;
    }

    private int heuristicValue(GameState whiteState, boolean player1) {
        GameState state;
        if (player1)
            state = whiteState;
        else
            state = whiteState.reversed();
        if (state.isWhiteWin())
            return Integer.MAX_VALUE;
        if (state.isRedWin())
            return Integer.MIN_VALUE;
        if (state.isDraw())
            return 0;

        int redPieces = 0;
        int whitePieces = 0;
        for (int i = 1; i <= state.cSquares; i++) {
            int cellValue = state.get(i);
            if ((cellValue & Constants.CELL_RED) != 0)
                redPieces++;
            if ((cellValue & Constants.CELL_WHITE) != 0)
                redPieces++;
        }
        return redPieces - whitePieces;
    }

    private int miniMax (GameState node, int depth, boolean maximizing) {
        if (depth == 0 || node.isEOG())
            return heuristicValue(node, maximizing); // Heuristic value of node (state)
        int bestValue;
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        if (maximizing) {
            bestValue = Integer.MIN_VALUE;
            for (GameState child : children) {
                int val = miniMax(child, depth - 1, false);
                bestValue = Math.max(bestValue, val);
            }
            return bestValue;
        } else {
            bestValue = Integer.MAX_VALUE;
            for (GameState child : children) {
                int val = miniMax(child, depth - 1, true);
                bestValue = Math.min(bestValue, val);
            }
            return bestValue;
        }
    }
}
