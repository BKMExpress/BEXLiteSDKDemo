package com.bkm.mobil.sdk.lite.demo.flow

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitsOnly = text.text.filter { it.isDigit() }.take(16)
        val formatted = digitsOnly.chunked(4).joinToString(" ")

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val digitsBefore = text.text.take(offset).count { it.isDigit() }
                val spaces = (digitsBefore - 1).coerceAtLeast(0) / 4
                return minOf(digitsBefore + spaces, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val spacesRemoved = offset - offset / 5
                return minOf(spacesRemoved, text.text.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
