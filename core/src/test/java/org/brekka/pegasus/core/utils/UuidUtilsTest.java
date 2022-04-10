package org.brekka.pegasus.core.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.junit.Test;

public class UuidUtilsTest {

    @Test
    public void toAndFrom() {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = UuidUtils.toBytes(uuid);
        UUID restored = UuidUtils.fromBytes(bytes);
        assertThat(restored, equalTo(uuid));
    }
}
