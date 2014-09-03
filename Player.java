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
            int value = miniMax(state, 30, true);
            if (value > bestValue) {
                bestValue = value;
                bestState = state;
            }
        }
        return bestState;
    }

    private int heuristicValue(GameState whiteState, boolean player1) {
        GameState state = null;
        if (player1)
            state = whiteState;
        else
            state = state.reversed();
        if (state.isWhiteWin())
            return Integer.MAX_VALUE;
        if (state.isRedWin())
            return Integer.MIN_VALUE;

        return 0;
    }

    private int miniMax (GameState node, int depth, boolean maximizing) {
        if (depth == 0 || node.isEOG())
            return heuristicValue(node, maximizing); // Heuristic value of node (state)
        int bestValue;
        if (maximizing) {
            bestValue = Integer.MIN_VALUE;
            Vector<GameState> children = new Vector<GameState>();
            for (GameState child : children) {
                int val = miniMax(child, depth - 1, false);
                bestValue = Math.max(bestValue, val);
            }
            return bestValue;
        } else {
            bestValue = Integer.MAX_VALUE;
            Vector<GameState> children = new Vector<GameState>();
            for (GameState child : children) {
                int val = miniMax(child, depth - 1, true);
                bestValue = Math.min(bestValue, val);
            }
            return bestValue;
        }
    }
}
