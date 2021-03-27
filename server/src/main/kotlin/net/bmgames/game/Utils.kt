package net.bmgames.game

import arrow.optics.Lens

fun <S, A> Lens<S, A>.modify(f: (A) -> A): (S) -> S = { s -> modify(s, f) }
fun <S, A> Lens<S, A>.set(a: A): (S) -> S = { s -> set(s, a) }

fun <A, B, C> Pair<A, B>.plus(c: C) = Triple(first, second, c)
fun <A, B, C> Pair<A, B>.map2(f: (B) -> C) = first to f(second)
