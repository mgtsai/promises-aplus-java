//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
//---------------------------------------------------------------------------------------------------------------------
final class ResolvedChain<PO> implements ResolveAction
{
    //-----------------------------------------------------------------------------------------------------------------
    private final PromiseFactory<PO> factory;
    private PromiseStore dstStore = null;
    private PO dstPromise = null;
    //-----------------------------------------------------------------------------------------------------------------
    ResolvedChain(final PromiseFactory<PO> factory)
    {
        this.factory = factory;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final void setAlwaysPending()
    {
        synchronized (this) {
            if (dstStore == null) {
                dstPromise = factory.alwaysPendingPromise();
                return;
            }
        }

        dstStore.setAlwaysPending();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final void setFulfilled(final Object value)
    {
        synchronized (this) {
            if (dstStore == null) {
                dstPromise = factory.fulfilledPromise(value);
                return;
            }
        }

        dstStore.setFulfilled(value);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final void setRejected(final Object reason, final Throwable exception)
    {
        synchronized (this) {
            if (dstStore == null) {
                dstPromise = factory.rejectedPromise(reason, exception);
                return;
            }
        }

        dstStore.setRejected(reason, exception);
    }
    //-----------------------------------------------------------------------------------------------------------------
    final synchronized PO dstPromise()
    {
        if (dstPromise != null)
            return dstPromise;
        else {
            dstStore = new PromiseStore();
            return factory.mutablePromise(dstStore);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
