package io.github.yusaka39.toolkt.container

import javax.lang.model.util.AbstractAnnotationValueVisitor7

open class Tuple<out T1, out T2, out T3, out T4, out T5, out T6, out T7, out T8, out T9, out T10>(
        val first: T1,
        val second: T2,
        val third: T3,
        val fourth: T4,
        val fifth: T5,
        val sixth: T6,
        val seventh: T7,
        val eighth: T8,
        val ninth: T9,
        val tenth: T10
) {
    operator fun component1(): T1 = this.first
    operator fun component2(): T2 = this.second
    operator fun component3(): T3 = this.third
    operator fun component4(): T4 = this.fourth
    operator fun component5(): T5 = this.fifth
    operator fun component6(): T6 = this.sixth
    operator fun component7(): T7 = this.seventh
    operator fun component8(): T8 = this.eighth
    operator fun component9(): T9 = this.ninth
    operator fun component10(): T10 = this.tenth
}

class Tuple9<out T1, out T2, out T3, out T4, out T5, out T6, out T7, out T8, out T9>(
        first: T1,
        second: T2,
        third: T3,
        fourth: T4,
        fifth: T5,
        sixth: T6,
        seventh: T7,
        eighth: T8,
        ninth: T9
) : Tuple<T1, T2, T3, T4, T5, T6, T7, T8, T9, Unit>(
        first,
        second,
        third,
        fourth,
        fifth,
        sixth,
        seventh,
        eighth,
        ninth,
        Unit
)

class Tuple8<out T1, out T2, out T3, out T4, out T5, out T6, out T7, out T8>(
        first: T1,
        second: T2,
        third: T3,
        fourth: T4,
        fifth: T5,
        sixth: T6,
        seventh: T7,
        eighth: T8
) : Tuple<T1, T2, T3, T4, T5, T6, T7, T8, Unit, Unit>(
        first,
        second,
        third,
        fourth,
        fifth,
        sixth,
        seventh,
        eighth,
        Unit,
        Unit
)

class Tuple7<out T1, out T2, out T3, out T4, out T5, out T6, out T7>(
        first: T1,
        second: T2,
        third: T3,
        fourth: T4,
        fifth: T5,
        sixth: T6,
        seventh: T7
) : Tuple<T1, T2, T3, T4, T5, T6, T7, Unit, Unit, Unit>(
        first,
        second,
        third,
        fourth,
        fifth,
        sixth,
        seventh,
        Unit,
        Unit,
        Unit
)

class Tuple6<out T1, out T2, out T3, out T4, out T5, out T6>(
        first: T1,
        second: T2,
        third: T3,
        fourth: T4,
        fifth: T5,
        sixth: T6
) : Tuple<T1, T2, T3, T4, T5, T6, Unit, Unit, Unit, Unit>(
        first,
        second,
        third,
        fourth,
        fifth,
        sixth,
        Unit,
        Unit,
        Unit,
        Unit
)

class Tuple5<out T1, out T2, out T3, out T4, out T5>(
        first: T1,
        second: T2,
        third: T3,
        fourth: T4,
        fifth: T5
) : Tuple<T1, T2, T3, T4, T5, Unit, Unit, Unit, Unit, Unit>(
        first,
        second,
        third,
        fourth,
        fifth,
        Unit,
        Unit,
        Unit,
        Unit,
        Unit
)

class Tuple4<out T1, out T2, out T3, out T4>(
        first: T1,
        second: T2,
        third: T3,
        fourth: T4
) : Tuple<T1, T2, T3, T4, Unit, Unit, Unit, Unit, Unit, Unit>(
        first,
        second,
        third,
        fourth,
        Unit,
        Unit,
        Unit,
        Unit,
        Unit,
        Unit
)

class Tuple3<out T1, out T2, out T3>(
        first: T1,
        second: T2,
        third: T3
) : Tuple<T1, T2, T3, Unit, Unit, Unit, Unit, Unit, Unit, Unit>(
        first,
        second,
        third,
        Unit,
        Unit,
        Unit,
        Unit,
        Unit,
        Unit,
        Unit
)

class Tuple2<out T1, out T2>(
        first: T1,
        second: T2
) : Tuple<T1, T2, Unit, Unit, Unit, Unit, Unit, Unit, Unit, Unit>(
        first,
        second,
        Unit,
        Unit,
        Unit,
        Unit,
        Unit,
        Unit,
        Unit,
        Unit
)

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> tuple(
        a1: T1,
        a2: T2,
        a3: T3,
        a4: T4,
        a5: T5,
        a6: T6,
        a7: T7,
        a8: T8,
        a9: T9,
        a10: T10
): Tuple<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> = Tuple(
        a1, a2, a3, a4, a5, a6, a7, a8, a9, a10
)

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> tuple(
        a1: T1,
        a2: T2,
        a3: T3,
        a4: T4,
        a5: T5,
        a6: T6,
        a7: T7,
        a8: T8,
        a9: T9
): Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> = Tuple9(
        a1, a2, a3, a4, a5, a6, a7, a8, a9
)

fun <T1, T2, T3, T4, T5, T6, T7, T8> tuple(
        a1: T1,
        a2: T2,
        a3: T3,
        a4: T4,
        a5: T5,
        a6: T6,
        a7: T7,
        a8: T8
): Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> = Tuple8(
        a1, a2, a3, a4, a5, a6, a7, a8
)

fun <T1, T2, T3, T4, T5, T6, T7> tuple(
        a1: T1,
        a2: T2,
        a3: T3,
        a4: T4,
        a5: T5,
        a6: T6,
        a7: T7
): Tuple7<T1, T2, T3, T4, T5, T6, T7> = Tuple7(
        a1, a2, a3, a4, a5, a6, a7
)

fun <T1, T2, T3, T4, T5, T6> tuple(
        a1: T1,
        a2: T2,
        a3: T3,
        a4: T4,
        a5: T5,
        a6: T6
): Tuple6<T1, T2, T3, T4, T5, T6> = Tuple6(
        a1, a2, a3, a4, a5, a6
)

fun <T1, T2, T3, T4, T5> tuple(
        a1: T1,
        a2: T2,
        a3: T3,
        a4: T4,
        a5: T5
): Tuple5<T1, T2, T3, T4, T5> = Tuple5(
        a1, a2, a3, a4, a5
)

fun <T1, T2, T3, T4> tuple(
        a1: T1,
        a2: T2,
        a3: T3,
        a4: T4
): Tuple4<T1, T2, T3, T4> = Tuple4(
        a1, a2, a3, a4
)

fun <T1, T2, T3> tuple(
        a1: T1,
        a2: T2,
        a3: T3
): Tuple3<T1, T2, T3> = Tuple3(
        a1, a2, a3
)

fun <T1, T2> tuple(
        a1: T1,
        a2: T2
): Tuple2<T1, T2> = Tuple2(
        a1, a2
)