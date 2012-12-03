/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.services.UploadPolicyService;
import org.brekka.pegasus.core.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Service
public class UploadPolicyServiceImpl implements UploadPolicyService {

    @Autowired
    private MemberService memberService;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.UploadPolicyService#identifyPolicy()
     */
    @Override
    public UploadPolicy identifyUploadPolicy() {
//        AuthenticatedMember current = memberService.getCurrent();
//        if (current == null) {
//            // TODO Return anonymous policy
//            return new UploadPolicyImpl(3, 5_000_000, 20_000_000);
//        } else {
//            // TODO member policy.
//            return new UploadPolicyImpl(200, 1_000_000_000, 2_000_000_000);
//        }
        // Same policy for all - for now
        return new UploadPolicyImpl(200, 1_000_000_000, 2_000_000_000, 1_000_000);
    }

}
