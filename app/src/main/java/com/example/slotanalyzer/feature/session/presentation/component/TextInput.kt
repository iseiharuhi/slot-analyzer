package com.example.slotanalyzer.feature.session.presentation.component

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember

@Composable
fun TextInput(
    value: Int?,
    onValueChange: (Int?) -> Unit
) {
    var text by remember(value) { mutableStateOf(value?.toString() ?: "") }

    LaunchedEffect(value) {
        val next = value?.toString() ?: ""
        if (text != next) text = next
    }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onValueChange(it.toIntOrNull())
        },
        placeholder = { Text("ー") },
        singleLine = true
    )
}
