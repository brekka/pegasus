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

package org.brekka.pegasus.web.pages;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tapestry5.annotations.Component;
import org.brekka.paveway.tapestry.components.Upload;

/**
 * TODO Description of Scratch
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class Scratch {

    @Component
    protected Upload tester;
    @Component
    protected Upload other;
    
    private String makeKey;
    
    Object onActivate(String makeKey) {
        tester.init(makeKey);
        other.init(makeKey);
        return Boolean.TRUE;
    }
    
    Object onPassivate() {
        return makeKey;
    }
    
    Object onSuccess() throws Exception {
        System.out.println(tester.getValue());
        makeKey = RandomStringUtils.randomAlphabetic(4);
        return true;
    }
}
