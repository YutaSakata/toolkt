package io.github.yusaka39.toolkt.container

abstract class Either<out L: Throwable, out R> {
    abstract val isRight: Boolean
    abstract fun assertRight(): R
    abstract fun rightOrThrowLeft(): R
    abstract fun rightOrNull(): R?
    abstract fun throwIfLeft()
    abstract fun assertLeft(): L
}

class Right<out L: Throwable, out R>(private val right: R): Either<L, R>() {
    override val isRight: Boolean = true
    override fun assertRight(): R = this.right
    override fun rightOrThrowLeft(): R = this.right
    override fun rightOrNull(): R? = this.right
    override fun throwIfLeft() = Unit
    override fun assertLeft(): L {
        throw IllegalStateException("You assert an Either is Left but it is Right")
    }
}

class Left<out L: Throwable, out R>(private val left: L): Either<L, R>() {
    override val isRight: Boolean = false
    override fun assertRight(): R {
        throw IllegalStateException("You assert an Either is Right but it is Left")
    }

    override fun rightOrThrowLeft(): R {
        throw this.left
    }
    override fun rightOrNull(): R? = null
    override fun throwIfLeft() {
        throw this.left
    }
    override fun assertLeft(): L = this.left
}

fun <L: Throwable, R> right(r: R) = Right<L, R>(r)
fun <L: Throwable, R> left(l: L) = Left<L,R>(l)