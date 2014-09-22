import java.util.*;

public class Player {
    boolean isRed = false;
    private GameState bestState;
    private static final int INFINITY = 1000000;
    private static final int NEG_INFINITY = -1000000;

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
        Vector<GameState> children = new Vector<GameState>();
        pState.findPossibleMoves(children);
        if (pState.getNextPlayer() == 1)
            isRed = true;
        else
            isRed = false;

        if (children.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(pState, new Move());
        }

        /**
         * Select best next state based on the negaMax algorithm
         */
        alphaBeta(pState, 11, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1);
        return bestState;
    }

    private int alphaBeta(GameState node, int depth, int alpha, int beta, int color) {
        if (depth == 0 || node.isEOG()) {
            return color * heuristicValue(node, depth);
        }
        int bestValue = -Integer.MAX_VALUE;
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        GameState bestChild = children.get(0);
        for (GameState child : children) {
            int val = -alphaBeta(child, depth - 1, -beta, -alpha, -color);
            if (val > bestValue) {
                bestValue = val;
                bestChild = child;
            }
            alpha = Math.max(alpha, val);
            if (alpha >= beta) {
                break;
            }
        }
        bestState = bestChild;
        return bestValue;
    }

    private int heuristicValue(GameState state, int depth) {
        if (isRed)
            state = state.reversed();

        if (state.isWhiteWin()) {
            return INFINITY * depth;
        }
        if (state.isRedWin()) {
            return NEG_INFINITY * depth;
        }
        if (state.isDraw()) {
            return 0;
        }

        int whitePawns = 0;
        int redPawns = 0;
        int whiteKings = 0;
        int redKings = 0;

        for (int i = 1; i <= state.cSquares; i++) {
            int cellValue = state.get(i);
            if (cellValue == Constants.CELL_WHITE)
                whitePawns++;
            else if (cellValue == Constants.CELL_RED)
                redPawns++;
            else if (cellValue == (Constants.CELL_WHITE | Constants.CELL_KING))
                whiteKings++;
            else if (cellValue == (Constants.CELL_RED | Constants.CELL_KING))
                redKings++;
        }
        int value = (whitePawns + whiteKings * 3) - (redPawns + redKings * 3);
        return value;
    }

}