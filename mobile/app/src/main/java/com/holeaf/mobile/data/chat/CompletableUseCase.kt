package com.holeaf.mobile.data.chat

import android.text.PrecomputedText
import io.reactivex.Completable


interface CompletableUseCase {
    fun getCompletable(params: PrecomputedText.Params? = null): Completable
}