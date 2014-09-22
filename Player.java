import java.util.*;

public class Player {
    boolean isRed = false;
    private GameState bestState;
    private long[][] zobrist = null;
    private Random random = new Random ();
    private static final int INFINITY = 1000000;
    private static final int NEG_INFINITY = -1000000;
    enum Flag {UPPERBOUND, LOWERBOUND, EXACT}
    HashMap<Long, TranspositionTableEntry> transpositionTable = new HashMap<Long, TranspositionTableEntry>();

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
//        if (children.size() == 1)
//            return children.firstElement();

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
        alphaBeta(pState, 11, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1);
//        System.err.println(getHash(pState));
//        alphaBetaZ(pState, 16, NEG_INFINITY, INFINITY, 1);
        return bestState;
//        return alphaBeta2(pState, 11, NEG_INFINITY, INFINITY, 1).state;
    }

    private int negaMax(GameState node, int depth, int color) {
        if (depth == 0 || node.isEOG()) {
            return color * heuristicValue(node, depth);
        }
        int bestValue = NEG_INFINITY;
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        GameState bestChild = children.get(0);
        for (GameState child : children) {
            int val = -negaMax(child, depth - 1, -color);
            if (val > bestValue) {
                bestValue = val;
                bestChild = child;
            }
        }
        bestState = bestChild;
        return bestValue;
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

    private int alphaBetaZ(GameState node, int depth, int alpha, int beta, int color) {
        int alphaOrig = alpha;
        long hash = getHash(node);
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
            return color * heuristicValue(node, depth);
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

    private long getHash (GameState state) {
        long hash = 0;
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
    private NegaResult negaMax2(GameState node, int depth, int color) {
        if (depth == 0 || node.isEOG()) {
            return new NegaResult(null, color * heuristicValue(node, depth));
        }
        int bestValue = Integer.MIN_VALUE;
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        NegaResult bestRes = new NegaResult(children.get(0), bestValue);
        for (GameState child : children) {
            int val = -negaMax2(child, depth - 1, -color).value;
            if (val > bestValue) {
                bestValue = val;
                bestRes = new NegaResult(child, val);
            }
        }
        return bestRes;
    }
    //
    private NegaResult alphaBeta2(GameState node, int depth, int alpha, int beta, int color) {
        if (depth == 0 || node.isEOG()) {
            return new NegaResult(null, color * heuristicValue(node, depth));
        }
        int bestValue = NEG_INFINITY;
        Vector<GameState> children = new Vector<GameState>();
        node.findPossibleMoves(children);
        NegaResult bestRes = new NegaResult(children.get(0), NEG_INFINITY);
        for (GameState child : children) {
            int val = -alphaBeta2(child, depth - 1, -beta, -alpha, -color).value;
            if (val > bestValue) {
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