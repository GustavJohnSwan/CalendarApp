package com.bignerdranch.android.calendarapp3.database_2.objectbox

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun BenchmarkScreen(
    onBack: () -> Unit
) {
    var benchmarkStatus by remember { mutableStateOf("Ready") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Button(onClick = onBack) { Text("Back") }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                Text(text = benchmarkStatus, style = MaterialTheme.typography.headlineSmall)
            }
            // Move your "BenchmarkSection" items here...
            item {
                Button(onClick = { /* Your existing benchmark code */ }) {
                    Text("Run Room Update")
                }
            }
        }
    }
}