/**
 * 
 */
package org.brekka.pegasus.core.model;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface AnonymousTransferKey extends TransferKey {
    String getToken();

    String getCode();
    
    String getFileName();
}
