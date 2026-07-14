package com.bignerdranch.android.calendarapp3.database_2.objectbox

import androidx.compose.foundation.layout.Spacer // Added
import androidx.compose.foundation.layout.height // Added
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
import androidx.compose.ui.unit.dp // Added for .dp units

@Composable
fun BenchmarkScreen(
    onBack: () -> Unit,
    viewModel: BBL_OB_CBL_Room = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var benchmarkStatus by remember { mutableStateOf("Ready") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {

            item {
                Button(onClick = onBack) {
                    Text("Back")
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Text(
                    text = benchmarkStatus,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                // The UI observes the ViewModel state directly
                Text(
                    text = viewModel.benchmarkStatus,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                Button(onClick = { viewModel.insertEntryBulk() }) {
                    Text("Run ObjectBox Bulk Insert")
                }
            }
        }
    }
}