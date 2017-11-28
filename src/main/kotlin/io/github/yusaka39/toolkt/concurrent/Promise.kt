package io.github.yusaka39.toolkt.concurrent

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
        get() = if (this.isFinished) _result as T else
            throw IllegalStateException("Execution has not finished yet.")

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