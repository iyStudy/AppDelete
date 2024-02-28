package com.example.deleteapp

import android.graphics.drawable.Drawable

data class AppData(val label: String,
                   val icon: Drawable,
                   val packageName: String,
                   var size: String = "",
                   var installTime: String = "",
                   var sizeBytes: Long = 0,
                   var isSelected: Boolean = false )

