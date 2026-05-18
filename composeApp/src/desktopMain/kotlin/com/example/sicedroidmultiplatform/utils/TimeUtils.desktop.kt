package com.example.sicedroidmultiplatform.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun nowFormatted(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
