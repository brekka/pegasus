/**
 * 
 */
package org.brekka.pegasus.core.model;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * A token must be marked with a type defined by this enumeration so that the system knows what part of the system is
 * using that token and can be redirected accordingly.
 * 
 * It is critical to note that the length of a token does not correspond to its type. There is just a default length
 * for auto-generated tokens of that type.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public enum PegasusTokenType implements TokenType {

    /**
     * Identifies anonymous uploads. Since a password has to be used in combination with anonymous uploads
     * they don't need a particularly long identifier
     */
    ANON(5),

    /**
     * For inboxes. Since all that is needed to upload to an inbox is this token, make it longer so that
     * it is more difficult to guess them. Given the available characters this should provide 22 billion
     * possible combinations.
     */
    INBOX(7),
    
    /**
     * An organization. Will normally be chosen rather than auto-generated.
     * 923k combinations
     */
    ORG(4),
    
    /**
     * Some other purpose - 831m combinations of random
     */
    OTHER(8),
    
    /**
     * A dispatch
     */
    DISPATCH(8),

    /**
     * Deposit
     */
    DEPOSIT(8),
    ;

    /**
     * The minimum length to be a valid token
     */
    public static final int MIN_LENGTH = 1;
    /**
     * The maximum supported length of tokens
     */
    public static final int MAX_LENGTH = 8;

    /**
     * The characters that can be used to auto-generate a token. Only upper case and numbers without
     * vowels. The exclusion of vowels is a simple attempt to avoid auto-generated profanity in URLs.
     * (31 chars).
     */
    private static final char[] ALLOWED = "BCDFGHJKLMNPQRSTVWXYZ1234567890".toCharArray();
    
    /**
     * The default length of tokens for the given type (though they don't have to be that length).
     */
    private final int generateLength;

    private PegasusTokenType(int tokenObj) {
        this.generateLength = tokenObj;
    }

    /**
     * @return the generateLength
     */
    public int getGenerateLength() {
        return generateLength;
    }

    public Token generateRandom() {
        String path = RandomStringUtils.random(getGenerateLength(), 0, ALLOWED.length, false, false, ALLOWED);
        Token token = new Token(path);
        token.setType(this);
        return token;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.TokenType#getKey()
     */
    @Override
    public String getKey() {
        return name();
    }

    /**
     * @param token
     * @return
     */
    public static TokenType identifyType(String token) {
        PegasusTokenType[] values = values();
        for (PegasusTokenType tokenType : values) {
            if (token.length() == tokenType.getGenerateLength()) {
                return tokenType;
            }
        }
        return null;
    }
}
