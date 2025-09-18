package com.quartier.quartier.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.quartier.quartier.R

//A component responsible for the styling of email fields

@Composable
fun EmailTextField(email: String, showError: Boolean, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        email, onValueChange = onValueChange,
        label = {Text(LocalContext.current.getString(R.string.email_label))},
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        supportingText = {
            if(showError) Text(LocalContext.current.getString(R.string.email_invalid))
        },
        isError = showError
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewEmailTextFieldValid() {
    EmailTextField(email = "email@gmail.com", showError = false) { }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmailTextFieldInvalid() {
    EmailTextField(email = "emailgmailcom", showError = true) { }
}