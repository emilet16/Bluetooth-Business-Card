package com.example.businesscard.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.businesscard.R

@Composable
fun EmailTextField(email: String, emailValid: Boolean, onValueChange: (String) -> Unit) {
    TextField(email, onValueChange = onValueChange, label = {Text(LocalContext.current.getString(R.string.email_label))}, singleLine = true,
        placeholder = {Text(LocalContext.current.getString(R.string.email_placeholder))},
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        supportingText = {
            if(!emailValid && email.isNotEmpty()) Text(LocalContext.current.getString(R.string.invalid_input))
        }, isError = !emailValid && email.isNotEmpty())
}