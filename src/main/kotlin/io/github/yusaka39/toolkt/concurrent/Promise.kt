package io.github.yusaka39.toolkt.concurrent

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write


fun <T> promise(procedure: () -> T): Promise<T> {

}

abstract class Promise<T> {

    enum class Status {
        INITIALIZED, REJECTED, RESOLVED
    }

    var status: Status = Status.INITIALIZED
    val isFinished: Boolean
        get() = this.status == Status.RESOLVED || this.status == Status.REJECTED

    abstract var result: T

    abstract protected fun resolve(result: T)
    abstract protected fun reject(e: Throwable)

    abstract fun <U> then(f: (T) -> U): Promise<U>
    abstract fun <U> then(promise: Promise<U>): Promise<U>
    abstract fun <U: Throwable> catch(handler: (U) -> Unit): Promise<T>
    abstract fun always(procedure: () -> Unit): Promise<T>

    abstract fun await(): Unit
    abstract fun interrupt(): Unit
}


internal class AsynchronousPromise<T>() : Promise<T>() {
    private var _result: T? = null
    override var result: T
        set(value) {
            this._result = value
        }
        get() = if (this.isFinished) _result as T else
            throw IllegalStateException("Execution has not finished yet.")

    private var thrown: Throwable? = null
    private val errorHandlers = mutableListOf<(Throwable) -> Unit>()
    private val finishHandlers = mutableListOf<() -> Unit>()

    private val lock = ReentrantReadWriteLock()

    override fun resolve(result: T) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reject(e: Throwable) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <U> then(f: (T) -> U): Promise<U> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <U> then(promise: Promise<U>): Promise<U> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <U : Throwable> catch(handler: (U) -> Unit): Promise<T> = this.apply {
        this.lock.read {
            if (this.status == Status.REJECTED) {

            } else {
                this.errorHandlers.add { (it as? U)?.let { handler(it) } }
            }
        }
    }

    override fun always(procedure: () -> Unit): Promise<T> = this.apply {
        this.finishHandlers.add(procedure)
    }

    override fun await() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun interrupt() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
//
//
//
//
//open class HogePromise<T : Any> internal constructor() {
//    constructor(procedure: () -> T) : this() {
//        this.resultGetter = procedure
//    }
//
//    var result: T? = null
//    protected lateinit var resultGetter: () -> T?
//    private val procedure: () -> Unit by lazy {
//        fun(): Unit {
//            try {
//                this.result = resultGetter()
//            } catch (e: Exception) {
//                onCatchCallback(e)
//            }
//        }
//    }
//    private var onCatchCallback: (Exception) -> Unit = { throw it }
//
//    open fun <U : Any> then(f: (T) -> U): Promise<U> {
//        val promise = Promise<U>()
//        promise.resultGetter = {
//            this.procedure()
//            val result = this.result
//            if (result == null) {
//                null
//            } else {
//                f(result)
//            }
//        }
//        return promise
//    }
//
//    open fun <U : Any> then(p: Promise<U>) : Promise<U> {
//        val promise = Promise<U>()
//        promise.resultGetter = {
//            this.procedure()
//            val result = this.result
//            if (result == null) {
//                null
//            } else {
//                p.procedure()
//                p.result
//            }
//        }
//        return promise
//    }
//
//    open fun thenOnUiThread(procedure: (T) -> Unit): Promise<Unit> = FinalizedPromise {
//        this.procedure()
//        val result = this.result ?: return@FinalizedPromise
//        this.runOnUiThread {
//            procedure(result)
//        }
//    }
//
//    fun <T: Exception> catch(errorHandler: (T) -> Unit) =
//            this.apply {
//                this.onCatchCallback = {
//                    try {
//                        errorHandler(it as T)
//                    } catch (e: ClassCastException) {
//                        throw it
//                    }
//                }
//            }
//
//    private fun runOnUiThread(procedure: () -> Unit) {
//        val handler = Handler(Looper.getMainLooper())
//        handler.post {
//            procedure()
//        }
//    }
//
//    fun sync() = this.apply { this.procedure() }
//    fun async() = this.apply { thread { this.procedure() } }
//}
//
//private class FinalizedPromise(procedure: () -> Unit) : Promise<Unit>() {
//    init {
//        this.resultGetter = procedure
//    }
//
//    override fun <U : Any> then(procedure: (Unit) -> U): Promise<U> {
//        throw IllegalStateException("The promise already finalized")
//    }
//
//    override fun thenOnUiThread(procedure: (Unit) -> Unit): FinalizedPromise {
//        throw IllegalStateException("The promise already finalized")
//    }
//}
//
//class PromiseAdapter<T : Any> : Promise<T> {
//    constructor(task: AbstractApi.Task<T>) : super(task.procedure)
//    constructor(task: AbstractDao.Task<T>) : super(task.procedure)
//}