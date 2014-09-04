package org.brekka.pegasus.core;

import org.brekka.commons.lang.ErrorCode;

/**
 * Error types relating to the Pegasus subsystem.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public enum PegasusErrorCode implements ErrorCode {

    PG100,
    PG101,
    PG102,
    PG103,
    PG104,
    PG105,
    /**
     * No user context available to resolve private keys in.
     */
    PG106,
    /**
     * Template not found
     */
    PG107,

    PG200,
    PG265,

    /**
     * Token already in use
     */
    PG300,
    /**
     * Incorrect unlock code for vault
     */
    PG302,
    PG333,

    PG400,
    PG401,
    PG402,
    PG403,
    /**
     * The resource has been deleted and is not longer available 'GONE'.
     */
    PG410,
    PG423,
    PG431,
    PG443,
    PG444,
    PG453,

    PG500,
    PG535,
    PG591,
    PG595,

    PG600,
    PG601,
    PG623,

    PG700,
    PG701,
    PG702,
    PG703,
    PG704,
    PG721,
    PG723,
    PG745,
    PG773,

    PG812,
    PG817,
    PG831,
    PG853,
    PG871,
    PG876,
    PG888,

    PG900,
    PG901,
    PG902,
    PG903,
    PG904,
    PG905,
    PG950,
    ;

    private static final Area AREA = ErrorCode.Utils.createArea("PG");
    private int number = 0;

    @Override
    public int getNumber() {
        return (this.number == 0 ? this.number = ErrorCode.Utils.extractErrorNumber(name(), getArea()) : this.number);
    }
    @Override
    public Area getArea() { return AREA; }
}
