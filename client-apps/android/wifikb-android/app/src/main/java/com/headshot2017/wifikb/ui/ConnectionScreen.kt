package com.headshot2017.wifikb.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.headshot2017.wifikb.ui.theme.WifikbClientTheme

@Composable
fun ConnectionScreen(
    navigation: NavController = rememberNavController(),
    modifier: Modifier = Modifier,
    viewModel: UDPViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) {
        viewModel.disconnect()
        navigation.popBackStack()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var text by remember { mutableStateOf("") }

        SelectionContainer {
            Text(
                text = uiState.textLog,
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            )
        }

        TextField(
            label = { Text("Keyboard entry") },
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    viewModel.send(text)
                    text = ""
                }
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionScreenPreview()
{
    WifikbClientTheme {
        ConnectionScreen()
    }
}
