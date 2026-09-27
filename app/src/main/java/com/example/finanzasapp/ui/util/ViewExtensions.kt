package com.example.finanzasapp.ui.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun View.applyTopSystemInset() {
    val initialTop = paddingTop

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top

        view.updatePadding(
            top = initialTop + topInset
        )

        insets
    }

    ViewCompat.requestApplyInsets(this)
}