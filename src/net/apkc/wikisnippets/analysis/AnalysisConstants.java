package net.apkc.wikisnippets.analysis;

/**
 * Token literal values and constants.
 *
 * @author K-Zen
 */
public interface AnalysisConstants {

    /**
     * End of File.
     */
    int EOF = 0;
    /**
     * RegularExpression Id.
     */
    int WORD = 1;
    /**
     * RegularExpression Id.
     */
    int ACRONYM = 2;
    /**
     * RegularExpression Id.
     */
    int SIGRAM = 3;
    /**
     * RegularExpression Id.
     */
    int IRREGULAR_WORD = 4;
    /**
     * RegularExpression Id.
     */
    int C_PLUS_PLUS = 5;
    /**
     * RegularExpression Id.
     */
    int C_SHARP = 6;
    /**
     * RegularExpression Id.
     */
    int PLUS = 7;
    /**
     * RegularExpression Id.
     */
    int MINUS = 8;
    /**
     * RegularExpression Id.
     */
    int QUOTE = 9;
    /**
     * RegularExpression Id.
     */
    int COLON = 10;
    /**
     * RegularExpression Id.
     */
    int SLASH = 11;
    /**
     * RegularExpression Id.
     */
    int DOT = 12;
    /**
     * RegularExpression Id.
     */
    int ATSIGN = 13;
    /**
     * RegularExpression Id.
     */
    int APOSTROPHE = 14;
    /**
     * RegularExpression Id.
     */
    int WHITE = 15;
    /**
     * RegularExpression Id.
     */
    int WORD_PUNCT = 16;
    /**
     * RegularExpression Id.
     */
    int LETTER = 17;
    /**
     * RegularExpression Id.
     */
    int CJK = 18;
    /**
     * RegularExpression Id.
     */
    int DIGIT = 19;
    /**
     * Lexical state.
     */
    int DEFAULT = 0;
    /**
     * Literal token values.
     */
    String[] tokenImage = {
        "<EOF>",
        "<WORD>",
        "<ACRONYM>",
        "<SIGRAM>",
        "<IRREGULAR_WORD>",
        "<C_PLUS_PLUS>",
        "<C_SHARP>",
        "\"+\"",
        "\"-\"",
        "\"\\\"\"",
        "\":\"",
        "\"/\"",
        "\".\"",
        "\"@\"",
        "\"\\\'\"",
        "<WHITE>",
        "<WORD_PUNCT>",
        "<LETTER>",
        "<CJK>",
        "<DIGIT>",};
}
