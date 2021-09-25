package org.brekka.pegasus.core.security;

public interface OtherAuthenticationDetails {
    String getRemoteAddress();

    String getUserAgent();
}
