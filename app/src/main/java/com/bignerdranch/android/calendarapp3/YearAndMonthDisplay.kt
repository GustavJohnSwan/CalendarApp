package com.bignerdranch.android.calendarapp3

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// this composable defines the style and content of the currently selected year and month display
@Composable
fun YearAndMonthDisplay(displayedMonth: String, displayedYear: Int) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = displayedYear.toString(),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )
    }

    Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = displayedMonth,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 25.sp
            )

    }
}