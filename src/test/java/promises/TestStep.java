//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
import promises.impl.ImplUtil;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
//---------------------------------------------------------------------------------------------------------------------
public final class TestStep
{
    //-----------------------------------------------------------------------------------------------------------------
    private final CountDownLatch pause = new CountDownLatch(1);
    private final CountDownLatch finish = new CountDownLatch(1);
    //-----------------------------------------------------------------------------------------------------------------
    public static <S, T> Return<S, T> retNoWait(final T ret)
    {
        return new Return<S, T>() {
            @Override public T call(final S self, final TestStep cbStep, final TestStep resStep) {
                cbStep.pause();
                resStep.finish();
                return ret;
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <S, T> Return<S, T> retThrowException(final Throwable exception)
    {
        return new Return<S, T>() {
            @Override public T
            call(final S self, final TestStep callbackStep, final TestStep resolveStep) throws Throwable {
                callbackStep.pause();
                resolveStep.finish();
                throw exception;
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <S, T> ReturnSupplier<S, T, T> suppIdentity()
    {
        return new ReturnSupplier<S, T, T>() {
            @Override public Return<S, T> get(final Return<S, ? extends T> retResolution) {
                return new Return<S, T>() {
                    @Override public T
                    call(final S self, final TestStep cbStep, final TestStep resStep) throws Throwable {
                        return ImplUtil.cast(retResolution.call(self, cbStep, resStep));
                    }
                };
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    public final TestStep pass()
    {
        pause.countDown();
        return this;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public final void pause()
    {
        try {
            pause.await(1, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            //
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    public final void finish()
    {
        finish.countDown();
    }
    //-----------------------------------------------------------------------------------------------------------------
    public final void sync()
    {
        pause.countDown();

        try {
            finish.await(1, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            //
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    public interface Return<S, T>
    {
        public abstract T call(final S self, final TestStep cbStep, final TestStep resStep) throws Throwable;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public interface ReturnSupplier<S, TI, TO>
    {
        public abstract Return<S, TO> get(final Return<S, ? extends TI> retResolution);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
