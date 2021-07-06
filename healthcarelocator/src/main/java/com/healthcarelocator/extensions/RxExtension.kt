package com.healthcarelocator.extensions

import io.reactivex.FlowableEmitter

fun <T> FlowableEmitter<T>.success(t: T) {
    if (!this.isCancelled) {
        this.onNext(t)
        this.onComplete()
    }
}