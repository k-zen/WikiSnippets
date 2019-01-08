package net.apkc.wikisnippets.analysis;

// IO
import java.io.PrintStream;
import java.io.Reader;

/**
 * Token Manager.
 *
 * @author K-Zen
 */
class WSAnalysisTokenManager implements AnalysisConstants {

    /** Debug output. */
    PrintStream debugStream = System.out;
    /** Token literal values. */
    static final String[] jjstrLiteralImages = {"", null, null, null, null, null, null, "\53", "\55", "\42", "\72", "\57", "\56", "\100", "\47", null, null, null, null, null,};
    static final long[] jjbitVec0 = {0xfffffffeL, 0x0L, 0x0L, 0x0L};
    static final long[] jjbitVec2 = {0x0L, 0x0L, 0x0L, 0xff7fffffff7fffffL};
    static final long[] jjbitVec3 = {0x1ff0000000000000L, 0xffffffffffffc000L, 0xffffffffL, 0x600000000000000L};
    static final long[] jjbitVec4 = {0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL};
    static final long[] jjbitVec5 = {0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffL, 0x0L};
    static final long[] jjbitVec6 = {0xffffffffffffffffL, 0xffffffffffffffffL, 0x0L, 0x0L};
    static final long[] jjbitVec7 = {0x3fffffffffffL, 0x0L, 0x0L, 0x0L};
    static final int[] jjnextStates = {7, 9,};
    /** Lexer state names. */
    static final String[] lexStateNames = {"DEFAULT",};
    CharStream inputStream;
    int[] jjrounds = new int[10];
    int[] jjstateSet = new int[20];
    StringBuffer jjimage = new StringBuffer();
    StringBuffer image = jjimage;
    int jjimageLen;
    int lengthOfMatch;
    char curChar;
    int curLexState = 0;
    int defaultLexState = 0;
    int jjnewStateCnt;
    int jjround;
    int jjmatchedPos;
    int jjmatchedKind;

    public WSAnalysisTokenManager(Reader reader) {
        this(new FastCharStream(reader));
    }

    public void setDebugStream(PrintStream ds) {
        this.debugStream = ds;
    }

    private int jjStopAtPos(int pos, int kind) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;

        return pos + 1;
    }

    private int jjMoveStringLiteralDfa0_0() {
        switch (this.curChar) {
            case 34:
                return jjStopAtPos(0, 9);
            case 39:
                return jjStopAtPos(0, 14);
            case 43:
                return jjStopAtPos(0, 7);
            case 45:
                return jjStopAtPos(0, 8);
            case 46:
                return jjStopAtPos(0, 12);
            case 47:
                return jjStopAtPos(0, 11);
            case 58:
                return jjStopAtPos(0, 10);
            case 64:
                return jjStopAtPos(0, 13);
            default:
                return jjMoveNfa_0(1, 0);
        }
    }

    private int jjMoveNfa_0(int startState, int curPos) {
        int startsAt = 0;
        this.jjnewStateCnt = 10;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = 0x7fffffff;

        for (;;) {
            if (++this.jjround == 0x7fffffff) {
                ReInitRounds();
            }

            if (this.curChar < 64) {
                long l = 1L << this.curChar;

                do {
                    switch (this.jjstateSet[--i]) {
                        case 1:
                        case 0:
                            if ((0x3ff004000000000L & l) == 0L) {
                                break;
                            }

                            kind = 1;
                            this.jjCheckNAdd(0);
                            break;
                        case 2:
                            if (this.curChar == 46) {
                                jjCheckNAdd(3);
                            }

                            break;
                        case 4:
                            if (this.curChar != 46) {
                                break;
                            }

                            if (kind > 2) {
                                kind = 2;
                            }

                            this.jjCheckNAdd(3);
                            break;
                        case 7:
                            if (this.curChar == 35) {
                                kind = 1;
                            }

                            break;
                        case 8:
                            if (this.curChar == 43 && kind > 1) {
                                kind = 1;
                            }

                            break;
                        case 9:
                            if (this.curChar == 43) {
                                this.jjstateSet[this.jjnewStateCnt++] = 8;
                            }

                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            }
            else if (this.curChar < 128) {
                long l = 1L << (this.curChar & 077);

                do {
                    switch (this.jjstateSet[--i]) {
                        case 1:
                            if ((0x7fffffe87fffffeL & l) != 0L) {
                                if (kind > 1) {
                                    kind = 1;
                                }

                                this.jjCheckNAdd(0);
                            }

                            if ((0x7fffffe07fffffeL & l) != 0L) {
                                this.jjstateSet[this.jjnewStateCnt++] = 2;
                            }

                            if ((0x800000008L & l) != 0L) {
                                this.jjAddStates(0, 1);
                            }

                            break;
                        case 0:
                            if ((0x7fffffe87fffffeL & l) == 0L) {
                                break;
                            }

                            if (kind > 1) {
                                kind = 1;
                            }

                            this.jjCheckNAdd(0);
                            break;
                        case 3:
                            if ((0x7fffffe07fffffeL & l) != 0L) {
                                this.jjstateSet[this.jjnewStateCnt++] = 4;
                            }

                            break;
                        case 6:
                            if ((0x800000008L & l) != 0L) {
                                this.jjAddStates(0, 1);
                            }

                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            }
            else {
                int hiByte = (this.curChar >> 8);
                int i1 = hiByte >> 6;
                long l1 = 1L << (hiByte & 077);
                int i2 = (this.curChar & 0xff) >> 6;
                long l2 = 1L << (this.curChar & 077);

                do {
                    switch (this.jjstateSet[--i]) {
                        case 1:
                            if (WSAnalysisTokenManager.jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                                if (kind > 1) {
                                    kind = 1;
                                }

                                this.jjCheckNAdd(0);
                            }

                            if (WSAnalysisTokenManager.jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                                this.jjstateSet[this.jjnewStateCnt++] = 2;
                            }

                            if (WSAnalysisTokenManager.jjCanMove_1(hiByte, i1, i2, l1, l2)) {
                                if (kind > 3) {
                                    kind = 3;
                                }
                            }

                            break;
                        case 0:
                            if (!WSAnalysisTokenManager.jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                                break;
                            }

                            if (kind > 1) {
                                kind = 1;
                            }

                            this.jjCheckNAdd(0);
                            break;
                        case 3:
                            if (WSAnalysisTokenManager.jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                                this.jjstateSet[this.jjnewStateCnt++] = 4;
                            }

                            break;
                        case 5:
                            if (WSAnalysisTokenManager.jjCanMove_1(hiByte, i1, i2, l1, l2) && kind > 3) {
                                kind = 3;
                            }

                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            }

            if (kind != 0x7fffffff) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = 0x7fffffff;
            }

            ++curPos;

            if ((i = this.jjnewStateCnt) == (startsAt = 10 - (this.jjnewStateCnt = startsAt))) {
                return curPos;
            }

            try {
                this.curChar = this.inputStream.readChar();
            }
            catch (java.io.IOException e) {
                return curPos;
            }
        }
    }

    private static boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2) {
        switch (hiByte) {
            case 0:
                return ((jjbitVec2[i2] & l2) != 0L);
            default:
                if ((jjbitVec0[i1] & l1) != 0L) {
                    return true;
                }

                return false;
        }
    }

    private static boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2) {
        switch (hiByte) {
            case 48:
                return ((jjbitVec4[i2] & l2) != 0L);
            case 49:
                return ((jjbitVec5[i2] & l2) != 0L);
            case 51:
                return ((jjbitVec6[i2] & l2) != 0L);
            case 61:
                return ((jjbitVec7[i2] & l2) != 0L);
            default:
                if ((jjbitVec3[i1] & l1) != 0L) {
                    return true;
                }

                return false;
        }
    }

    public WSAnalysisTokenManager(CharStream stream) {
        this.inputStream = stream;
    }

    public WSAnalysisTokenManager(CharStream stream, int lexState) {
        this(stream);
        this.SwitchTo(lexState);
    }

    /**
     * Reinitialise parser.
     *
     * @param stream
     */
    public void ReInit(CharStream stream) {
        this.jjmatchedPos = this.jjnewStateCnt = 0;
        this.curLexState = this.defaultLexState;
        this.inputStream = stream;
        this.ReInitRounds();
    }

    private void ReInitRounds() {
        int i;
        this.jjround = 0x80000001;

        for (i = 10; i-- > 0;) {
            this.jjrounds[i] = 0x80000000;
        }
    }

    /**
     * Reinitialise parser.
     *
     * @param stream
     * @param lexState
     */
    public void ReInit(CharStream stream, int lexState) {
        this.ReInit(stream);
        this.SwitchTo(lexState);
    }

    /**
     * Switch to specified lex state.
     *
     * @param lexState
     */
    public void SwitchTo(int lexState) {
        if (lexState >= 1 || lexState < 0) {
            throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
        }
        else {
            this.curLexState = lexState;
        }
    }

    protected Token jjFillToken() {
        final Token t;
        final String curTokenImage;
        final int beginLine;
        final int endLine;
        final int beginColumn;
        final int endColumn;
        String im = WSAnalysisTokenManager.jjstrLiteralImages[this.jjmatchedKind];
        curTokenImage = (im == null) ? this.inputStream.GetImage() : im;
        beginLine = this.inputStream.getBeginLine();
        beginColumn = this.inputStream.getBeginColumn();
        endLine = this.inputStream.getEndLine();
        endColumn = this.inputStream.getEndColumn();
        t = Token.newToken(this.jjmatchedKind, curTokenImage);
        t.beginLine = beginLine;
        t.endLine = endLine;
        t.beginColumn = beginColumn;
        t.endColumn = endColumn;

        return t;
    }

    /**
     * Get the next Token.
     *
     * @return
     */
    public Token getNextToken() {
        Token matchedToken;
        int curPos;

        EOFLoop:
        for (;;) {
            try {
                this.curChar = this.inputStream.BeginToken();
            }
            catch (java.io.IOException e) {
                this.jjmatchedKind = 0;
                matchedToken = this.jjFillToken();

                return matchedToken;
            }

            this.image = this.jjimage;
            this.image.setLength(0);
            this.jjimageLen = 0;
            this.jjmatchedKind = 0x7fffffff;
            this.jjmatchedPos = 0;
            curPos = this.jjMoveStringLiteralDfa0_0();

            if (this.jjmatchedPos == 0 && this.jjmatchedKind > 15) {
                this.jjmatchedKind = 15;
            }

            if (this.jjmatchedKind != 0x7fffffff) {
                if (this.jjmatchedPos + 1 < curPos) {
                    this.inputStream.backup(curPos - this.jjmatchedPos - 1);
                }

                matchedToken = this.jjFillToken();
                this.TokenLexicalActions(matchedToken);

                return matchedToken;
            }

            int errorLine = this.inputStream.getEndLine();
            int errorColumn = this.inputStream.getEndColumn();
            String errorAfter = null;
            boolean EOFSeen = false;

            try {
                this.inputStream.readChar();
                this.inputStream.backup(1);
            }
            catch (java.io.IOException e1) {
                EOFSeen = true;
                errorAfter = curPos <= 1 ? "" : this.inputStream.GetImage();

                if (this.curChar == '\n' || this.curChar == '\r') {
                    errorLine++;
                    errorColumn = 0;
                }
                else {
                    errorColumn++;
                }
            }

            if (!EOFSeen) {
                this.inputStream.backup(1);
                errorAfter = curPos <= 1 ? "" : this.inputStream.GetImage();
            }

            throw new TokenMgrError(EOFSeen, this.curLexState, errorLine, errorColumn, errorAfter, this.curChar, TokenMgrError.LEXICAL_ERROR);
        }
    }

    void TokenLexicalActions(Token matchedToken) {
        switch (this.jjmatchedKind) {
            case 1:
                this.image.append(this.inputStream.GetSuffix(this.jjimageLen + (this.lengthOfMatch = this.jjmatchedPos + 1)));
                matchedToken.image = matchedToken.image.toLowerCase();
                break;
            case 2:
                this.image.append(this.inputStream.GetSuffix(this.jjimageLen + (this.lengthOfMatch = this.jjmatchedPos + 1)));

                // remove dots
                for (int i = 0; i < this.image.length(); i++) {
                    if (this.image.charAt(i) == '.') {
                        this.image.deleteCharAt(i--);
                    }
                }

                matchedToken.image = this.image.toString().toLowerCase();
                break;
            default:
                break;
        }
    }

    private void jjCheckNAdd(int state) {
        if (this.jjrounds[state] != this.jjround) {
            this.jjstateSet[this.jjnewStateCnt++] = state;
            this.jjrounds[state] = this.jjround;
        }
    }

    private void jjAddStates(int start, int end) {
        do {
            this.jjstateSet[this.jjnewStateCnt++] = WSAnalysisTokenManager.jjnextStates[start];
        } while (start++ != end);
    }
}
