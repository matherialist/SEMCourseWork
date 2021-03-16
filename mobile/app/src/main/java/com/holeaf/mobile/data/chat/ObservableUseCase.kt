package com.holeaf.mobile.data.chat

import android.text.PrecomputedText
import io.reactivex.Observable


interface ObservableUseCase<T> {
    fun getObservable(params: PrecomputedText.Params? = null): Observable<T>
}