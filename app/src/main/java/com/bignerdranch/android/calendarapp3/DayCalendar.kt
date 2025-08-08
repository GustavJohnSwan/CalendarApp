package com.bignerdranch.android.calendarapp3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import java.time.LocalDate
import androidx.compose.ui.unit.sp


// this composable is responsible for the style of the calendar month view content in general
// and both the style of the CURRENT DATE and the SELECTED DATE
@Composable
fun Day(onDateClick: () -> Unit, day: CalendarDay, isSelected: Boolean, onDateSelect: (String) -> Unit, onClick: (CalendarDay) -> Unit) {
    // stores the current date
    val currentDate = LocalDate.now()

    Box(
        // CURRENT DATE and SELECTED DATE style
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                color = if (isSelected) {
                    Color.Blue
                } else if (day.date == currentDate) {
                    Color.Gray
                } else Color.Transparent

            )

            // makes every date clickable in the CURRENTLY SELECTED MONTH
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = {
                    if (day.position == DayPosition.MonthDate) {
                        onDateSelect(day.date.toString())
                    }
                    onClick(day)
                    onDateClick()
                          },
            ),
        contentAlignment = Alignment.Center
    ) {




        // text style for the SELECTED DATE
        if (day.position == DayPosition.MonthDate && isSelected) {

            Text(
                text = day.date.dayOfMonth.toString(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            // text style for the CURRENT DATE
        } else if (day.position == DayPosition.MonthDate && day.date == currentDate) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            // text style for all CURRENTLY DISPLAYED MONTH DATES
        } else if (day.position == DayPosition.MonthDate) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = Color.Black
            )

            // text style for all CURRENTLY DISPLAYED OTHER MONTH DATES
        } else {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = Color.Gray
            )
        }
    }
}