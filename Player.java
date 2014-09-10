import java.util.*;

public class Player {
    boolean isRed = false;
    private GameState bestState;
    private long[][] zobrist = null;
    private Random random = new Random ();
    private static final int INFINITY = Integer.MAX_VALUE;
    private static final int NEG_INFINITY = -Integer.MAX_VALUE;
    enum Flag {UPPERBOUND, LOWERBOUND, EXACT}
    HashMap<Integer, TranspositionTableEntry> transpositionTable = new HashMap<Integer, TranspositionTableEntry>();

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
        if (children.size() == 1)
            return children.firstElement();

        /**
         * Select best next state based on the negaMax algorithm
         */
//        System.err.println(alphaBeta(pState, 9, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1).value);
//        System.err.println(negaMax(pState, 9, 1).value);
//        return negaMax2(pState, 9, 1).state;
//        return alphaBeta2(pState, 9, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1).state;

//        negaMax(pState, 9, 1);
//        transpositionTable.clear();
//        initZobrist();
        alphaBeta(pState, 11, NEG_INFINITY, INFINITY, 1);
//        System.err.println(getHash(pState));
//        alphaBetaZ(pState, 11, NEG_INFINITY, INFINITY, 1);
        return bestState;
    }

    private int negaMax(GameState node, int depth, int color) {
        if (depth == 0 || node.isEOG()) {
            return color * heuristicValue(node);
        }
        int bestValue = NEG_INFINITY;
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        GameState bestChild = null;
        for (GameState child : children) {
            int val = -negaMax(child, depth - 1, -color);
            if (val >= bestValue) {
                bestValue = val;
                bestChild = child;
            }
        }
        bestState = bestChild;
        return bestValue;
    }

    private int alphaBeta(GameState node, int depth, int alpha, int beta, int color) {
        if (depth == 0 || node.isEOG()) {
            return color * heuristicValue(node);
        }
        int bestValue = NEG_INFINITY;
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

    private int alphaBetaZ(GameState node, int depth, int alpha, int beta, int color) {
        int alphaOrig = alpha;
        int hash = getHash(node);
        if (transpositionTable.containsKey(hash)) {
            TranspositionTableEntry entry = transpositionTable.get(hash);
            if (entry.depth >= depth) {
                if (entry.flag == Flag.EXACT)
                    return entry.value;
                else if (entry.flag == Flag.LOWERBOUND)
                    alpha = Math.max(alpha, entry.value);
                else if (entry.flag == Flag.UPPERBOUND)
                    beta = Math.min(beta, entry.value);
                if (alpha >= beta)
                    return entry.value;
            }
        }

        if (depth == 0 || node.isEOG()) {
            return color * heuristicValue(node);
        }
        int bestValue = NEG_INFINITY;
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        GameState bestChild = children.get(0);
        for (GameState child : children) {
            int val = -alphaBetaZ(child, depth - 1, -beta, -alpha, -color);
            if (val > bestValue) {
                bestValue = val;
                bestChild = child;
            }
            alpha = Math.max(alpha, val);
            if (alpha >= beta) {
                break;
            }
        }
        TranspositionTableEntry newEntry = new TranspositionTableEntry();
        newEntry.value = bestValue;
        if (bestValue <= alphaOrig)
            newEntry.flag = Flag.UPPERBOUND;
        else if (bestValue >= beta)
            newEntry.flag = Flag.LOWERBOUND;
        else
            newEntry.flag = Flag.EXACT;
        newEntry.depth = depth;
        transpositionTable.put(hash, newEntry);
        bestState = bestChild;
        return bestValue;
    }

    private void initZobrist () {
        zobrist = new long [GameState.cSquares][4];
        for (int i = 0; i < GameState.cSquares; i++){
            for (int j = 0; j < 4; j++) {
                zobrist[i][j] = random.nextLong();
            }
        }
    }

    private int getHash (GameState state) {
        int hash = 0;
        for (int i = 1; i <= state.cSquares; i++) {
            int cellValue = state.get(i);
            if (cellValue != 0) {
                hash ^= zobrist[i-1][getPieceIndex(cellValue)];
            }
        }
        return hash;
    }

    private int getPieceIndex(int type) {
        int index = 0;
        switch (type) {
            case Constants.CELL_WHITE: index = 0;
                break;
            case Constants.CELL_WHITE | Constants.CELL_KING: index = 1;
                break;
            case Constants.CELL_RED: index = 2;
                break;
            case Constants.CELL_RED | Constants.CELL_KING: index = 3;
                break;
            default: System.exit(0);
                break;
        }
        return index;

    }
//    private NegaResult negaMax2(GameState node, int depth, int color) {
//        if (depth == 0 || node.isEOG()) {
//            return new NegaResult(null, color * heuristicValue(node));
//        }
//        int bestValue = Integer.MIN_VALUE;
//        Vector<GameState> children = new Vector<GameState>();
//        node.findPossibleMoves(children);
//        NegaResult bestRes = null;
//        for (GameState child : children) {
//            int val = -negaMax2(child, depth - 1, -color).value;
//            if (val >= bestValue) {
//                bestValue = val;
//                bestRes = new NegaResult(child, val);
//            }
//        }
//        return bestRes;
//    }
//
//    private NegaResult alphaBeta2(GameState node, int depth, int alpha, int beta, int color) {
//        if (depth == 0 || node.isEOG()) {
//            return new NegaResult(null, color * heuristicValue(node));
//        }
//        int bestValue = -Integer.MAX_VALUE;
//        Vector<GameState> children = new Vector<GameState>();
//        node.findPossibleMoves(children);
//        NegaResult bestRes = new NegaResult(null, -Integer.MAX_VALUE);
//        for (GameState child : children) {
//            int val = -alphaBeta2(child, depth - 1, -beta, -alpha, -color).value;
//            if (val >= bestValue) {
//                bestValue = val;
//                bestRes = new NegaResult(child, val);
//            }
//            alpha = Math.max(alpha, val);
//            if (alpha >= beta) {
//                break;
//            }
//        }
//        return bestRes;
//    }

    private int heuristicValue(GameState state) {
        if (isRed)
            state = state.reversed();

        if (state.isWhiteWin()) {
            return INFINITY;
        }
        if (state.isRedWin()) {
            return NEG_INFINITY;
        }
        if (state.isDraw()) {
            return NEG_INFINITY + 1;
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
        int value = (whitePawns * 2 + whiteKings * 3) - (redPawns * 2 + redKings * 3);
        return value;
        // Pawn value
//        int whitePawns = numberOfType(state, Constants.CELL_WHITE);
//        value += getPawnValue(whitePawns);
//        int redPawns = numberOfType(state, Constants.CELL_RED);
//        value -= getPawnValue(redPawns);
//
//        // King value
//        int whiteKings = numberOfType(state, Constants.CELL_WHITE | Constants.CELL_KING);
//        value += getKingValue(whiteKings);
//        int redKings = numberOfType(state, Constants.CELL_RED | Constants.CELL_KING);
//        value -= getKingValue(redKings);

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


    }

//    private int getPawnValue(int numberOfPawns) {
//        return numberOfPawns * 2;
//    }
//
//    private int getKingValue (int numberOfKings) {
//        return numberOfKings * 3;
//    }
//
//    private int getSafePawnValue (int numberOfSafePawns) {
//        return numberOfSafePawns * 1;
//    }
//
//    private int getSafeKingValue (int numberOfSafeKings) {
//        return numberOfSafeKings * 1;
//    }
//
//    private int getPromotionDistanceValue (int promotionDistance) {
//        return promotionDistance * 3;
//    }
//
//    private int numberOfType(GameState state, int type) {
//        int count = 0;
//        for (int i = 1; i <= state.cSquares; i++) {
//            int cellValue = state.get(i);
//            if (cellValue == type)
//                count++;
//        }
//        return count;
//    }
//
//    private int safeNumberOfType(GameState state, int type) {
//        int count = 0;
//        for (int i = 1; i <= state.cSquares; i++) {
//            int cellValue = state.get(i);
//            if (cellValue == type && (state.cellToCol(i) == 0 || state.cellToCol(i) == 7
//                    || state.cellToRow(i) == 0 || state.cellToRow(i) == 7))
//                count++;
//        }
//        return count;
//    }
//
//    private int totalPromotionDistance (GameState state, int type) {
//        assert (type == Constants.CELL_RED || type == Constants.CELL_WHITE);
//        int distance = 0;
//        for (int i = 1; i <= state.cSquares; i++) {
//            int cellValue = state.get(i);
//            if (cellValue == type)
//                distance += promotionDistance(state, i, type);
//        }
//        return distance;
//    }
//
//    private int promotionDistance (GameState state, int cell, int type) {
//        if (type == Constants.CELL_WHITE) {
//            return state.cellToRow(cell);
//        } else {
//            // Red pawn
//            return 7 - state.cellToRow(cell);
//        }
//    }
}