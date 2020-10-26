package com.pavelkrylov.vsafe.base

interface MyRequest<out T> {
    fun run(): T
}