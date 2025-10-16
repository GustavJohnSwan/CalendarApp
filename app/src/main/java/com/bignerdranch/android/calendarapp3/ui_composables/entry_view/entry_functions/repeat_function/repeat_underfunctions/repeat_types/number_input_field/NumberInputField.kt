package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types.number_input_field

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

// Add this composable at the top of the file, before DailyRepeatOptions
@Composable
fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    maxLength: Int = 4,
    onFocusLost: (() -> Unit)? = null
) {
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }
    var hasFocus by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            // Allow only digits and limit length
            val newText = newValue.text.filter { char -> char.isDigit() }.take(maxLength)
            textFieldValue = newValue.copy(text = newText)
            onValueChange(newText)
        },
        modifier = modifier
            .onFocusChanged { focusState ->
                if (focusState.isFocused && !hasFocus) {
                    // Select all text when gaining focus
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                } else if (hasFocus && !focusState.isFocused) {
                    // Validate when losing focus
                    onFocusLost?.invoke()
                }
                hasFocus = focusState.isFocused
            },
        label = { if (label.isNotEmpty()) Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}
