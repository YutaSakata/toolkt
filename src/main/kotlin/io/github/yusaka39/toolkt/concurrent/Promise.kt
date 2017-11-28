package io.github.yusaka39.toolkt.concurrent

import io.github.yusaka39.toolkt.container.Either
import io.github.yusaka39.toolkt.container.left
import io.github.yusaka39.toolkt.container.right
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write


fun <T> promise(isAsync: Boolean = true, procedure: () -> T): Promise<T> = if (isAsync) {
    AsynchronousPromise(procedure).apply { this.invoke() }
} else {
    SynchronousPromise(procedure).apply { this.invoke() }
}

abstract class Promise<T> {

    enum class Status {
        INITIALIZED, REJECTED, RESOLVED
    }

    var status: Status = Status.INITIALIZED
    val isFinished: Boolean
        get() = this.status == Status.RESOLVED || this.status == Status.REJECTED

    abstract var result: T

    abstract internal fun invoke()
    abstract internal fun resolve(result: T)
    abstract internal fun reject(e: Throwable?)

    abstract fun <U> then(f: (T) -> U): Promise<U>
    abstract fun <U> then(promise: Promise<U>): Promise<U>
    abstract fun <U: Throwable> catch(handler: (U) -> Unit): Promise<T>
    abstract fun always(procedure: () -> Unit): Promise<T>

    @Deprecated("This method do nothing")
    fun async() = Unit
    abstract fun await()
    abstract fun throwUncaught()
}


internal abstract class BasePromise<T>(protected val procedure: () -> T) : Promise<T>() {

    override var result: T
        set(value) {
            this._result = value
        }
        get() = when (this.status) {
            Status.INITIALIZED -> throw IllegalStateException("Execution has not finished yet.")
            Status.REJECTED -> throw this.unCaughtThrowable ?:
                    IllegalStateException("Trying to get result from rejected promise")
            Status.RESOLVED -> _result as T
        }

    protected val finishHandlers = mutableListOf<() -> Unit>()

    private var _result: T? = null

    private var next: Promise<*>? = null
    private var unCaughtThrowable: Throwable? = null
    private val errorHandlers = mutableListOf<(Throwable) -> Boolean>()
    private val lock = ReentrantReadWriteLock()

    private val latch = CountDownLatch(1)



    override fun resolve(result: T) {
        this.lock.write {
            if (this.status != Status.INITIALIZED) {
                return
            }
            this.status = Status.RESOLVED
        }
        this.result = result
        this.latch.countDown()
        this.next?.invoke()
    }

    override fun reject(e: Throwable?) {
        this.lock.write {
            if (this.status != Status.INITIALIZED) {
                return
            }
            this.status = Status.REJECTED
            e?.let {
                this.unCaughtThrowable = e
                this.errorHandlers.forEach {
                    if (it(e)) {
                        this.unCaughtThrowable = null
                        return@let
                    }
                }
            }
        }
        this.latch.countDown()
        this.next?.reject(this.unCaughtThrowable)
    }

    override fun <U> then(f: (T) -> U): Promise<U> {
        val next = SynchronousPromise { f(this.result) }
        return this.then(next)
    }

    override fun <U> then(promise: Promise<U>): Promise<U> {
        this.next = promise

        this.lock.read {
            when (this.status) {
                Status.RESOLVED -> promise.invoke()
                Status.REJECTED -> promise.reject(this.unCaughtThrowable)
                else -> {}
            }
        }
        return promise
    }

    override fun <U : Throwable> catch(handler: (U) -> Unit): Promise<T> = this.apply {
        this.lock.read {
            @Suppress("unchecked_cast")
            if (this.status == Status.REJECTED) {
                try {
                    val e = this.unCaughtThrowable as U
                    handler(e)
                    this.unCaughtThrowable = null
                } catch (e: ClassCastException) {
                    // Ignore
                }
            } else {
                this.errorHandlers.add {
                    try {
                        val e = it as U
                        handler(e)
                        true
                    } catch (e: ClassCastException) {
                        false
                    }
                }
            }
        }
    }

    override fun always(procedure: () -> Unit): Promise<T> = this.apply {
        this.finishHandlers.add(procedure)
    }

    override fun await() {
        this.latch.await()
    }

    override fun throwUncaught() {
        this.unCaughtThrowable?.let {
            throw it
        }
    }
}


internal class AsynchronousPromise<T>(procedure: () -> T) : BasePromise<T>(procedure) {
    override fun invoke() {
        thread {
            try {
                this.resolve(this.procedure())
            } catch (e: Throwable) {
                this.reject(e)
            } finally {
                this.finishHandlers.forEach { it() }
            }
        }
    }
}

internal class SynchronousPromise<T>(procedure: () -> T) : BasePromise<T>(procedure) {
    override fun invoke() {
        try {
            this.resolve(this.procedure())
        } catch (e: Throwable) {
            this.reject(e)
        } finally {
            this.finishHandlers.forEach { it() }
        }
    }
}

private fun <T> Promise<T>.awaitAndGetResult(): Either<Throwable, T> {
    this.await()
    return try {
        this.throwUncaught()
        right(this.result)
    } catch (t: Throwable) {
        left(t)
    }
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, U> `when`(
        p1: Promise<T1>,
        p2: Promise<T2>,
        p3: Promise<T3>,
        p4: Promise<T4>,
        p5: Promise<T5>,
        p6: Promise<T6>,
        p7: Promise<T7>,
        p8: Promise<T8>,
        p9: Promise<T9>,
        p10: Promise<T10>,
        f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> U): Promise<U> = promise {
    f(p1.awaitAndGetResult().rightOrThrowLeft(),
      p2.awaitAndGetResult().rightOrThrowLeft(),
      p3.awaitAndGetResult().rightOrThrowLeft(),
      p4.awaitAndGetResult().rightOrThrowLeft(),
      p5.awaitAndGetResult().rightOrThrowLeft(),
      p6.awaitAndGetResult().rightOrThrowLeft(),
      p7.awaitAndGetResult().rightOrThrowLeft(),
      p8.awaitAndGetResult().rightOrThrowLeft(),
      p9.awaitAndGetResult().rightOrThrowLeft(),
      p10.awaitAndGetResult().rightOrThrowLeft())
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, U> `when`(
        p1: Promise<T1>,
        p2: Promise<T2>,
        p3: Promise<T3>,
        p4: Promise<T4>,
        p5: Promise<T5>,
        p6: Promise<T6>,
        p7: Promise<T7>,
        p8: Promise<T8>,
        p9: Promise<T9>,
        f: (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> U): Promise<U> = promise {
    f(p1.awaitAndGetResult().rightOrThrowLeft(),
      p2.awaitAndGetResult().rightOrThrowLeft(),
      p3.awaitAndGetResult().rightOrThrowLeft(),
      p4.awaitAndGetResult().rightOrThrowLeft(),
      p5.awaitAndGetResult().rightOrThrowLeft(),
      p6.awaitAndGetResult().rightOrThrowLeft(),
      p7.awaitAndGetResult().rightOrThrowLeft(),
      p8.awaitAndGetResult().rightOrThrowLeft(),
      p9.awaitAndGetResult().rightOrThrowLeft())
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, U> `when`(
        p1: Promise<T1>,
        p2: Promise<T2>,
        p3: Promise<T3>,
        p4: Promise<T4>,
        p5: Promise<T5>,
        p6: Promise<T6>,
        p7: Promise<T7>,
        p8: Promise<T8>,
        f: (T1, T2, T3, T4, T5, T6, T7, T8) -> U): Promise<U> = promise {
    f(p1.awaitAndGetResult().rightOrThrowLeft(),
      p2.awaitAndGetResult().rightOrThrowLeft(),
      p3.awaitAndGetResult().rightOrThrowLeft(),
      p4.awaitAndGetResult().rightOrThrowLeft(),
      p5.awaitAndGetResult().rightOrThrowLeft(),
      p6.awaitAndGetResult().rightOrThrowLeft(),
      p7.awaitAndGetResult().rightOrThrowLeft(),
      p8.awaitAndGetResult().rightOrThrowLeft())
}

fun <T1, T2, T3, T4, T5, T6, T7, U> `when`(
        p1: Promise<T1>,
        p2: Promise<T2>,
        p3: Promise<T3>,
        p4: Promise<T4>,
        p5: Promise<T5>,
        p6: Promise<T6>,
        p7: Promise<T7>,
        f: (T1, T2, T3, T4, T5, T6, T7) -> U): Promise<U> = promise {
    f(p1.awaitAndGetResult().rightOrThrowLeft(),
      p2.awaitAndGetResult().rightOrThrowLeft(),
      p3.awaitAndGetResult().rightOrThrowLeft(),
      p4.awaitAndGetResult().rightOrThrowLeft(),
      p5.awaitAndGetResult().rightOrThrowLeft(),
      p6.awaitAndGetResult().rightOrThrowLeft(),
      p7.awaitAndGetResult().rightOrThrowLeft())
}

fun <T1, T2, T3, T4, T5, T6, U> `when`(
        p1: Promise<T1>,
        p2: Promise<T2>,
        p3: Promise<T3>,
        p4: Promise<T4>,
        p5: Promise<T5>,
        p6: Promise<T6>,
        f: (T1, T2, T3, T4, T5, T6) -> U): Promise<U> = promise {
    f(p1.awaitAndGetResult().rightOrThrowLeft(),
      p2.awaitAndGetResult().rightOrThrowLeft(),
      p3.awaitAndGetResult().rightOrThrowLeft(),
      p4.awaitAndGetResult().rightOrThrowLeft(),
      p5.awaitAndGetResult().rightOrThrowLeft(),
      p6.awaitAndGetResult().rightOrThrowLeft())
}

fun <T1, T2, T3, T4, T5, U> `when`(
        p1: Promise<T1>,
        p2: Promise<T2>,
        p3: Promise<T3>,
        p4: Promise<T4>,
        p5: Promise<T5>,
        f: (T1, T2, T3, T4, T5) -> U): Promise<U> = promise {
    f(p1.awaitAndGetResult().rightOrThrowLeft(),
      p2.awaitAndGetResult().rightOrThrowLeft(),
      p3.awaitAndGetResult().rightOrThrowLeft(),
      p4.awaitAndGetResult().rightOrThrowLeft(),
      p5.awaitAndGetResult().rightOrThrowLeft())
}

fun <T1, T2, T3, T4, U> `when`(
        p1: Promise<T1>,
        p2: Promise<T2>,
        p3: Promise<T3>,
        p4: Promise<T4>,
        f: (T1, T2, T3, T4) -> U): Promise<U> = promise {
    f(p1.awaitAndGetResult().rightOrThrowLeft(),
      p2.awaitAndGetResult().rightOrThrowLeft(),
      p3.awaitAndGetResult().rightOrThrowLeft(),
      p4.awaitAndGetResult().rightOrThrowLeft())
}

fun <T1, T2, T3, U> `when`(
        p1: Promise<T1>,
        p2: Promise<T2>,
        p3: Promise<T3>,
        f: (T1, T2, T3) -> U): Promise<U> = promise {
    f(p1.awaitAndGetResult().rightOrThrowLeft(), p2.awaitAndGetResult().rightOrThrowLeft(), p3.awaitAndGetResult().rightOrThrowLeft())
}

fun <T1, T2, U> `when`(p1: Promise<T1>, p2: Promise<T2>, f: (T1, T2) -> U): Promise<U> = promise {
    f(p1.awaitAndGetResult().rightOrThrowLeft(), p2.awaitAndGetResult().rightOrThrowLeft())
}

fun <T1, U> `when`(promise: Promise<T1>, f: (T1) -> U): Promise<U> = promise {
    f(promise.awaitAndGetResult().rightOrThrowLeft())
}



