package com.rainmachine.presentation.util

import com.rainmachine.R
import com.rainmachine.data.util.DataException
import io.reactivex.exceptions.CompositeException
import io.reactivex.functions.Consumer
import timber.log.Timber


object GenericErrorDealer : Consumer<Throwable> {

    override fun accept(throwable: Throwable) {
        if (!isExpected(throwable)) {
            if (throwable is CompositeException) {
                val exceptions = throwable.exceptions
                for (exception in exceptions) {
                    if (!isExpected(exception)) {
                        Timber.e(throwable, "Unexpected composite error: %s", throwable
                                .message)
                        break
                    }
                }
            } else {
                /* If we reach here then it means this is a completely unexpected error and we
                notify headquarters */
                Timber.e(throwable, "Unexpected error: %s", throwable.message)
            }
        } else {
            if (throwable is DataException) {
                showDataExceptionMessage(throwable)
            } else if (throwable is CompositeException) {
                val exceptions = throwable.exceptions
                exceptions
                        .filterIsInstance<DataException>()
                        .forEach { showDataExceptionMessage(it) }
            }

            Timber.d(throwable, "Filtered error: %s", throwable.message)
        }
    }

    private fun showDataExceptionMessage(dataException: DataException) {
        if (dataException.status == DataException.Status.SPRINKLER_ERROR) {
            Toasts.show(R.string.all_error_rain_machine, dataException.sprinklerErrorMessage)
        }
    }

    private fun isExpected(throwable: Throwable): Boolean {
        if (throwable is DataException) {
            return throwable.status != DataException.Status.UNKNOWN
        }
        if (throwable is CompositeException) {
            val exceptions = throwable.exceptions
            exceptions
                    .filter { it is DataException && it.status != DataException.Status.UNKNOWN }
                    .forEach { return true }
        }
        return false
    }
}