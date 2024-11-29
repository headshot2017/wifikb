package com.headshot2017.wifikb.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.headshot2017.wifikb.Routes
import com.headshot2017.wifikb.ui.theme.WifikbClientTheme

@Composable
fun MainMenuScreen(
    navigation: NavController = rememberNavController(),
    modifier: Modifier = Modifier,
    viewModel: UDPViewModel = viewModel()
) {
    val activity = (LocalContext.current as Activity)
    var ipDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 25.dp, vertical = 250.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.connect("255.255.255.255", false)
                navigation.navigate(Routes.CONNECTION)
            }
        ) {
            Text("Find DS automatically")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                ipDialog = true
            }
        ) {
            Text("Enter IP address...")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.connect("255.255.255.255", true)
                navigation.navigate(Routes.CONNECTION)
            }
        ) {
            Text("Reverse connection mode (for emulators)")
        }
    }

    if (ipDialog)
    {
        var ipStr by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { ipDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OutlinedTextField(
                        label = { Text("DS IP address") },
                        value = ipStr,
                        onValueChange = { ipStr = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Go,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                if (ipStr.isNotEmpty())
                                {
                                    ipDialog = false
                                    viewModel.connect(ipStr, false)
                                    navigation.navigate(Routes.CONNECTION)
                                }
                                else
                                    Toast.makeText(
                                        activity,
                                        "Please enter a valid IP address",
                                        Toast.LENGTH_LONG
                                    ).show()
                            }
                        ),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        TextButton(
                            onClick = {
                                ipDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }

                        TextButton(
                            onClick = {
                                if (ipStr.isNotEmpty())
                                {
                                    ipDialog = false
                                    viewModel.connect(ipStr, false)
                                    navigation.navigate(Routes.CONNECTION)
                                }
                                else
                                    Toast.makeText(
                                        activity,
                                        "Please enter a valid IP address",
                                        Toast.LENGTH_LONG
                                    ).show()
                            }
                        ) {
                            Text("Connect")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview()
{
    WifikbClientTheme {
        MainMenuScreen()
    }
}
