package com.example.businesscard.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.businesscard.R

@Composable
fun EmailTextField(email: String, showError: Boolean, onValueChange: (String) -> Unit) {
    TextField(email, onValueChange = onValueChange, label = {Text(LocalContext.current.getString(R.string.email_label))}, singleLine = true,
        placeholder = {Text(LocalContext.current.getString(R.string.email_placeholder))},
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        supportingText = {
            if(showError) Text(LocalContext.current.getString(R.string.invalid_input))
        }, isError = showError)
}

@Preview
@Composable
fun PreviewEmailTextFieldValid() {
    EmailTextField(email = "email@gmail.com", showError = false) { }
}

@Preview
@Composable
fun PreviewEmailTextFieldInvalid() {
    EmailTextField(email = "emailgmailcom", showError = true) { }
}