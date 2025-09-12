package com.bignerdranch.android.calendarapp3.entry_extra_data

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckBoxRepeatEvent() {
    var checked1 by remember { mutableStateOf(false) }
    var checked2 by remember { mutableStateOf(false) }
    var checked3 by remember { mutableStateOf(false) }
    var checked4 by remember { mutableStateOf(false) }
    var checked5 by remember { mutableStateOf(false) }

    Column {

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(//modifier = Modifier.weight(1f)
                    Modifier.width(150.dp)) {
                Text(
                    "Don't repeat"
                )
            }

            Column {
            Checkbox(
                checked = checked1,
                onCheckedChange = { checked1 = it }
            )
                }
        }


        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(//modifier = Modifier.weight(1f)
                Modifier.width(150.dp) ) {
                Text(
                    "Every day"
                )
            }

            Column {
                Checkbox(
                    checked = checked2,
                    onCheckedChange = { checked2 = it }
                )
            }
        }


        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = //Modifier.weight(1f)
                    Modifier.width(150.dp)) {
                Text(
                    "Every week"
                )
            }

            Column {
                Checkbox(
                    checked = checked3,
                    onCheckedChange = { checked3 = it }
                )
            }
        }


        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = //Modifier.weight(1f)
                    Modifier.width(150.dp)) {
                Text(
                    "Every month"
                )
            }

            Column {
                Checkbox(
                    checked = checked4,
                    onCheckedChange = { checked4 = it }
                )
            }
        }


        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(//modifier = Modifier.weight(1f)
                    Modifier.width(150.dp)) {
                Text(
                    "Every year"
                )
            }

            Column {
                Checkbox(
                    checked = checked5,
                    onCheckedChange = { checked5 = it }
                )
            }
        }
    }


    Row {
        Text(
        if (checked1) "Checkbox is checked" else "Checkbox is unchecked"
    )
    }

}