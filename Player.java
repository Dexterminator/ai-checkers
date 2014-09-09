import java.util.*;

public class Player {
    boolean isRed = false;
    private GameState bestState;

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
//        System.err.println(alphaBeta(pState, 9, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1).value);
//        System.err.println(negaMax(pState, 9, 1).value);
//        return negaMax2(pState, 9, 1).state;
//        return alphaBeta2(pState, 9, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1).state;
//        bestState = null;
//        negaMax(pState, 9, 1);
        alphaBeta(pState, 9, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
        return bestState;
    }

    private int negaMax(GameState node, int depth, int color) {
        if (depth == 0 || node.isEOG()) {
            return color * heuristicValue(node);
        }
        int bestValue = Integer.MIN_VALUE;
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        GameState bestchild = null;
        for (GameState child : children) {
            int val = -negaMax(child, depth - 1, -color);
            if (val >= bestValue) {
                bestValue = val;
                bestchild = child;
            }
        }
        bestState = bestchild;
        return bestValue;
    }

    private int alphaBeta(GameState node, int depth, int alpha, int beta, int color) {
        if (depth == 0 || node.isEOG()) {
            return color * heuristicValue(node);
        }
        int bestValue = Integer.MIN_VALUE;
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        GameState bestChild = null;
        for (GameState child : children) {
            int val = -alphaBeta(child, depth - 1, -beta, -alpha, -color);
            if (val >= bestValue) {
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

    private NegaResult negaMax2(GameState node, int depth, int color) {
        if (depth == 0 || node.isEOG()) {
            return new NegaResult(null, color * heuristicValue(node));
        }
        int bestValue = Integer.MIN_VALUE;
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        NegaResult bestRes = null;
        for (GameState child : children) {
            int val = -negaMax2(child, depth - 1, -color).value;
            if (val >= bestValue) {
                bestValue = val;
                bestRes = new NegaResult(child, val);
            }
        }
        return bestRes;
    }

    private NegaResult alphaBeta2(GameState node, int depth, int alpha, int beta, int color) {
        if (depth == 0 || node.isEOG()) {
            return new NegaResult(null, color * heuristicValue(node));
        }
        int bestValue = -Integer.MAX_VALUE;
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        NegaResult bestRes = new NegaResult(null, -Integer.MAX_VALUE);
        for (GameState child : children) {
            int val = -alphaBeta2(child, depth - 1, -beta, -alpha, -color).value;
            if (val >= bestValue) {
                bestValue = val;
                bestRes = new NegaResult(child, val);
            }
            alpha = Math.max(alpha, val);
            if (alpha >= beta) {
                break;
            }
        }
        return bestRes;
    }

    private int heuristicValue(GameState state) {
        if (isRed)
            state = state.reversed();

        if (state.isWhiteWin()) {
            return Integer.MAX_VALUE;
        }
        if (state.isRedWin()) {
            return Integer.MIN_VALUE;
        }
        if (state.isDraw()) {
            return Integer.MIN_VALUE;
        }

        int value = 0;

        // Pawn value
        int whitePawns = numberOfType(state, Constants.CELL_WHITE);
        value += getPawnValue(whitePawns);
        int redPawns = numberOfType(state, Constants.CELL_RED);
        value -= getPawnValue(redPawns);

        // King value
        int whiteKings = numberOfType(state, Constants.CELL_WHITE | Constants.CELL_KING);
        value += getKingValue(whiteKings);
        int redKings = numberOfType(state, Constants.CELL_RED | Constants.CELL_KING);
        value -= getKingValue(redKings);

        // Safe pawn value
//        int safeWhitePawns = safeNumberOfType(state, Constants.CELL_WHITE);
//        value += getSafePawnValue(safeWhitePawns);
//        int safeRedPawns = safeNumberOfType(state, Constants.CELL_RED);
//        value -= getSafePawnValue(safeRedPawns);
//
//        // Safe king value
//        int safeWhiteKings = safeNumberOfType(state, Constants.CELL_WHITE | Constants.CELL_KING);
//        value += getSafeKingValue(safeWhiteKings);
//        int safeRedKings = safeNumberOfType(state, Constants.CELL_RED | Constants.CELL_KING);
//        value -= getSafeKingValue(safeRedKings);

        // Promotion distance value
//        int whitePromotionDistance = totalPromotionDistance(state, Constants.CELL_WHITE);
//        value -= getPromotionDistanceValue(whitePromotionDistance);
//        int redPromotionDistance = totalPromotionDistance(state, Constants.CELL_RED);
//        value += getPromotionDistanceValue(redPromotionDistance);

        return value;
    }

    private int getPawnValue(int numberOfPawns) {
        return numberOfPawns * 2;
    }

    private int getKingValue (int numberOfKings) {
        return numberOfKings * 3;
    }

    private int getSafePawnValue (int numberOfSafePawns) {
        return numberOfSafePawns * 1;
    }

    private int getSafeKingValue (int numberOfSafeKings) {
        return numberOfSafeKings * 1;
    }

    private int getPromotionDistanceValue (int promotionDistance) {
        return promotionDistance * 3;
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

    private int safeNumberOfType(GameState state, int type) {
        int count = 0;
        for (int i = 1; i <= state.cSquares; i++) {
            int cellValue = state.get(i);
            if (cellValue == type && (state.cellToCol(i) == 0 || state.cellToCol(i) == 7
                    || state.cellToRow(i) == 0 || state.cellToRow(i) == 7))
                count++;
        }
        return count;
    }

    private int totalPromotionDistance (GameState state, int type) {
        assert (type == Constants.CELL_RED || type == Constants.CELL_WHITE);
        int distance = 0;
        for (int i = 1; i <= state.cSquares; i++) {
            int cellValue = state.get(i);
            if (cellValue == type)
                distance += promotionDistance(state, i, type);
        }
        return distance;
    }

    private int promotionDistance (GameState state, int cell, int type) {
        if (type == Constants.CELL_WHITE) {
            return state.cellToRow(cell);
        } else {
            // Red pawn
            return 7 - state.cellToRow(cell);
        }
    }
}