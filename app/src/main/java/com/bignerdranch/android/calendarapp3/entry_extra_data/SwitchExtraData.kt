package com.bignerdranch.android.calendarapp3.entry_extra_data

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable

@Composable
fun SwitchExtraData(isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    //var checked by remember { mutableStateOf(false) }

    Switch(
        checked = isChecked,
        onCheckedChange = onCheckedChange

    )
}