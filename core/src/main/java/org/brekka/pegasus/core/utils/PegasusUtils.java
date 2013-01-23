/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.pegasus.core.utils;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;

/**
 * General purpose Pegasus utility methods
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public final class PegasusUtils {

    private PegasusUtils() {
        // Non cons
    }

    /**
     * Cast the specified value to the expected type, throwing a {@link PegasusException} if the value is not of the
     * expected type (as determined by {@link Class#isAssignableFrom(Class)}.
     * 
     * @param value
     *            the value to check, can be null in which case null is simply returned.
     * @param expectedType
     *            the expected type, must NOT be null
     * @param errorCode
     *            the error code to include in the exception, if thrown.
     * @return the value assuming it is assignable from the expected type.
     * @throws PegasusException
     *             if the value is assignable to the expected type.
     */
    public static <T> T asType(Object value, Class<T> expectedType, PegasusErrorCode errorCode) {
        return asType(value, expectedType, errorCode, null);
    }

    /**
     * Cast the specified value to the expected type, throwing a {@link PegasusException} if the value is not of the
     * expected type (as determined by {@link Class#isAssignableFrom(Class)}.
     * 
     * @param value
     *            the value to check, can be null in which case null is simply returned.
     * @param expectedType
     *            the expected type, must NOT be null
     * @param errorCode
     *            the error code to include in the exception, if thrown.
     * @param errorMessagePrefix
     *            a message prefix to include with the exception thrown
     * @param errorMessageArgs
     *            args to go with the message prefix.
     * @return the value assuming it is assignable from the expected type.
     * @throws PegasusException
     *             if the value is assignable to the expected type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(Object value, Class<T> expectedType, PegasusErrorCode errorCode,
            String errorMessagePrefix, Object... errorMessageArgs) {
        if (value == null) {
            return null;
        }
        if (expectedType == null) {
            throw new IllegalArgumentException("The 'expectedType' is required and cannot be null");
        }
        Class<?> actualType = value.getClass();
        if (expectedType.isAssignableFrom(actualType)) {
            return (T) value;
        }
        String expectedClassName = expectedType.getName();
        String actualClassName = actualType.getName();
        String message;
        Object[] args;
        if (errorMessagePrefix != null) {
            String errorMessageCombined = String.format(errorMessagePrefix, errorMessageArgs);
            message = "%s: expected '%s', actual '%s'";
            args = new Object[] { errorMessageCombined, expectedClassName, actualClassName };
        } else {
            message = "Expected '%s', actual '%s'";
            args = new Object[] { expectedClassName, actualClassName };
        }
        throw new PegasusException(errorCode, message, args);
    }

    /**
     * Ensure that the specified parameter is not null. If null is found a {@link PegasusException} will be thrown with
     * the that includes the parameter name in the message, and the error code {@link PegasusErrorCode#PG950}.
     * 
     * @param value
     *            the value to check for null.
     * @param parameterName
     *            the name of the parameter
     */
    public static void checkNotNull(Object value, String parameterName) {
        checkNotNull(parameterName, "parameterName");
        if (value == null) {
            throw new PegasusException(PegasusErrorCode.PG950, "The parameter '%s' is required", parameterName);
        }
    }

}
