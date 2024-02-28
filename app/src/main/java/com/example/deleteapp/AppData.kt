package com.example.deleteapp

import android.graphics.drawable.Drawable

data class AppData(val label: String,// アプリ名
                   val icon: Drawable,// アプリアイコン
                   val packageName: String,// パッケージ名
                   var size: String = "",// 表示用のアプリサイズ: ○○B, ○○MB , ○○GB
                   var installTime: String = "",//インストール日時
                   var sizeBytes: Long = 0, // 並び替え用アプリサイズ
                   var isSelected: Boolean = false // 選択フラグ
                )

