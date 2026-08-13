package com.bkm.mobil.sdk.lite.demo.flow

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.Calendar

private fun expiryYearBounds(): Pair<Int, Int> {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR) % 100
    val maxYear = (currentYear + 10) % 100
    return currentYear to maxYear
}

private fun validateExpiryMonth(month: String, fallback: String): String = when {
    month.toIntOrNull() == null -> fallback
    month.toInt() > 12 -> "12"
    month.toInt() < 1 && month.length == 2 -> "01"
    else -> month
}

private fun validateExpiryYear(year: String, currentYear: Int, maxYear: Int): String = when {
    year.length == 2 && year.toIntOrNull() != null -> {
        val yearInt = year.toInt()
        when {
            maxYear < currentYear -> {
                if (yearInt >= currentYear || yearInt <= maxYear) {
                    year
                } else {
                    if (yearInt - maxYear < currentYear - yearInt) {
                        maxYear.toString().padStart(2, '0')
                    } else {
                        currentYear.toString().padStart(2, '0')
                    }
                }
            }

            else -> when {
                yearInt > maxYear -> maxYear.toString().padStart(2, '0')
                yearInt < currentYear -> currentYear.toString().padStart(2, '0')
                else -> year
            }
        }
    }

    else -> year
}

class ExpiryDateVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digitsOnly = text.text.filter { it.isDigit() }.take(4)
        val (currentYear, maxYear) = expiryYearBounds()
        val formatted = when {
            digitsOnly.length >= 3 -> {
                val month = digitsOnly.substring(0, 2)
                val year = digitsOnly.substring(2)
                "${validateExpiryMonth(month, digitsOnly)}/${
                    validateExpiryYear(
                        year,
                        currentYear,
                        maxYear
                    )
                }"
            }

            digitsOnly.isNotEmpty() -> {
                if (digitsOnly.length >= 2) {
                    validateExpiryMonth(digitsOnly.substring(0, 2), digitsOnly)
                } else {
                    digitsOnly
                }
            }

            else -> ""
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val digitsBefore = text.text.take(offset).count { it.isDigit() }
                return when {
                    digitsBefore <= 2 -> digitsBefore
                    digitsBefore <= 4 -> digitsBefore + 1
                    else -> formatted.length
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset == 3 -> 2
                    offset <= 5 -> offset - 1
                    else -> text.text.length
                }
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }

    companion object {
        fun normalizeExpiryDigits(raw: String): String {
            val digitsOnly = raw.filter { it.isDigit() }.take(4)
            if (digitsOnly.isEmpty()) return ""
            val (currentYear, maxYear) = expiryYearBounds()
            return when {
                digitsOnly.length >= 3 -> {
                    val month = digitsOnly.substring(0, 2)
                    val year = digitsOnly.substring(2)
                    validateExpiryMonth(month, month) + validateExpiryYear(
                        year,
                        currentYear,
                        maxYear
                    )
                }

                digitsOnly.length == 2 -> validateExpiryMonth(
                    digitsOnly.substring(0, 2),
                    digitsOnly
                )

                else -> digitsOnly
            }
        }
    }
}
