package org.brekka.pegasus.core;

import org.brekka.commons.lang.BaseException;


/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class PegasusException extends BaseException {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -2718288490183266743L;

    /**
     * @param errorCode
     * @param message
     * @param messageArgs
     */
    public PegasusException(PegasusErrorCode errorCode, String message, Object... messageArgs) {
        super(errorCode, message, messageArgs);
    }

    /**
     * @param errorCode
     * @param cause
     * @param message
     * @param messageArgs
     */
    public PegasusException(PegasusErrorCode errorCode, Throwable cause, String message, Object... messageArgs) {
        super(errorCode, cause, message, messageArgs);
    }

}
