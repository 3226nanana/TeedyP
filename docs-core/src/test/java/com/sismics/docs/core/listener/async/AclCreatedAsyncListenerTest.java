package com.sismics.docs.core.listener.async;

import com.sismics.docs.BaseTransactionalTest;
import com.sismics.docs.core.event.AclCreatedAsyncEvent;
import com.sismics.docs.core.util.TransactionUtil;
import org.junit.Test;
import com.sismics.docs.core.constant.PermType; // Ensure this is the correct package for PermType

public class AclCreatedAsyncListenerTest extends BaseTransactionalTest {

    @Test
    public void createAclShouldNotFail() throws Exception {
        // prepare transaction boundary
        TransactionUtil.commit();

        // fire the event
        AclCreatedAsyncListener listener = new AclCreatedAsyncListener();
        AclCreatedAsyncEvent event = new AclCreatedAsyncEvent();
        event.setSourceId("doc-123");
        event.setPerm(PermType.READ);
        event.setTargetId("user-456");
        listener.on(event);

        // if we reach here without exception, listener behaved as expected
    }

}


