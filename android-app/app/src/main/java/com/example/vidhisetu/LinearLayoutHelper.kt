package com.example.vidhisetu

import android.content.Context
import android.widget.LinearLayout

object LinearLayoutHelper {

    fun createVertical(
        context: Context
    ): LinearLayout {

        return LinearLayout(context).apply {

            orientation =
                LinearLayout.VERTICAL

            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
        }
    }
}