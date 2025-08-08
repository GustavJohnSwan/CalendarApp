package com.bignerdranch.android.calendarapp3

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SwitchExtraData(isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    //var checked by remember { mutableStateOf(false) }

    Switch(
        checked = isChecked,
        onCheckedChange = onCheckedChange

    )
}