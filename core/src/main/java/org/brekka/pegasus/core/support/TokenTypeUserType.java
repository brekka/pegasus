/*
 * Copyright 2012 the original author or authors.
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

package org.brekka.pegasus.core.support;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.model.PegasusTokenType;
import org.brekka.pegasus.core.model.TokenType;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

/**
 * Severity User Type
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class TokenTypeUserType implements UserType {

    private static final int[] TYPES = { Types.VARCHAR };

    private final Map<String, TokenType> typesMap;

    public TokenTypeUserType() {
        this(Arrays.asList(PegasusTokenType.class));
    }

    public TokenTypeUserType(Map<String, TokenType> typesMap) {
        this.typesMap = new HashMap<>(typesMap);
    }

    public <T extends Enum<?> & TokenType> TokenTypeUserType(List<Class<T>> enumTypesList) {
        Map<String, TokenType> typesMap = new HashMap<>();
        for (Class<T> tClass : enumTypesList) {
            T[] enumConstants = tClass.getEnumConstants();
            for (T t : enumConstants) {
                TokenType existing = typesMap.put(t.getKey(), t);
                if (existing != null) {
                    throw new PegasusException(PegasusErrorCode.PG595,
                            "Unable to add token type key '%s' for enum '%s', the key is already taken by '%s'",
                            existing.getKey(), t.getClass().getName(), existing.getClass().getName());
                }
            }
        }
        this.typesMap = typesMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    @Override
    public int[] sqlTypes() {
        return TYPES;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    @Override
    public Class<TokenType> returnedClass() {
        return TokenType.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x.equals(y);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
     */
    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[],
     * org.hibernate.engine.spi.SessionImplementor, java.lang.Object)
     */
    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        String value = rs.getString(names[0]);
        if (value == null) {
            return null;
        }
        TokenType tokenType = typesMap.get(value);
        if (tokenType == null) {
            throw new PegasusException(PegasusErrorCode.PG831, "No token type found for key '%s'", value);
        }
        return tokenType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int,
     * org.hibernate.engine.spi.SessionImplementor)
     */
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
            return;
        }
        TokenType tokenType = (TokenType) value;
        st.setObject(index, tokenType.getKey(), Types.VARCHAR);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    @Override
    public boolean isMutable() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return value.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original.toString();
    }
}
