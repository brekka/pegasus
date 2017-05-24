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

package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Connection;
import org.brekka.pegasus.core.model.KeySafe;

/**
 * TODO Description of ConnectionDAO
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface ConnectionDAO extends EntityDAO<UUID, Connection<?, ?, ?>>  {

    /**
     * @param keySafe
     * @param contextMember
     * @return
     */
    List<Connection<?, ?, ?>> identifyConnectionsBetween(KeySafe<?> keySafe, Actor contextMember);

    <Target extends KeySafe<?>, T extends Connection<Actor, KeySafe<? extends Actor>, Target>> List<T>
            retrieveConnectionsByTarget(Target target, Class<T> expected);

    /**
     * @param vault
     */
    void deleteWithSourceKeySafe(KeySafe<?> keySafe);

    /**
     * @param associate
     */
    void deleteWithOwner(Actor owner);

    Connection<?, ?, ?> retrieveBySurrogate(Actor owner, KeySafe<?> source, KeySafe<?> target);

}
