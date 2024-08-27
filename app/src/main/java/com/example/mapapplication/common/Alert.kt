package com.example.mapapplication.common

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class Alert(
    val title: String?,
    val description: String?,
    val alertContext: Context,
    val positiveMsg: String?,
    val negativeMsg: String?,
    val positiveButton: ((DialogInterface) -> Unit)?,
    val negativeButton: ((DialogInterface) -> Unit)?
) {

    constructor(builder: Builder) : this(
        builder.title,
        builder.description,
        builder.alertContext,
        builder.positiveMsg,
        builder.negativeMsg,
        builder.positiveButton,
        builder.negativeButton
    )

    init {
        val alert = AlertDialog.Builder(alertContext)
            .setTitle(title)
            .setMessage(description)
            .setPositiveButton(positiveMsg ?: alertContext.getString(android.R.string.ok))
            { dialog, _ -> positiveButton?.invoke(dialog) }

        if (negativeButton != null) {
            alert.setNegativeButton(negativeMsg ?: alertContext.getString(android.R.string.cancel))
            { dialog, _ ->
                negativeButton.invoke(dialog)
            }
        }

        alert.show()
    }

    companion object {
        inline fun alert(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        var title: String? = null
        var description: String? = null
        lateinit var alertContext: Context
        var positiveMsg: String? = null
        var negativeMsg: String? = null
        var positiveButton: ((DialogInterface) -> Unit)? = null
        var negativeButton: ((DialogInterface) -> Unit)? = null
        fun build() = Alert(this)
    }
}
