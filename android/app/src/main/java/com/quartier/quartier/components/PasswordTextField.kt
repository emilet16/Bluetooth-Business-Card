package com.quartier.quartier.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.quartier.quartier.R

//A component responsible for the styling of password input fields

@Composable
fun PasswordTextField(pwd: String, showError: Boolean, onValueChange: (String) -> Unit) {
    var showPwd by rememberSaveable { mutableStateOf(false) }

    TextField(value = pwd, onValueChange = onValueChange, label = {Text(LocalContext.current.getString(R.string.password_label))}, singleLine = true, placeholder = {Text(LocalContext.current.getString(R.string.password_placeholder))},
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        visualTransformation = if(showPwd) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if(showPwd) painterResource(R.drawable.outline_visibility_off_24)
            else painterResource(R.drawable.outline_visibility_24)

            val description = if(showPwd) R.string.hide_password else R.string.show_password

            IconButton({ showPwd = !showPwd }) {
                Icon(icon, LocalContext.current.getString(description))
            }
        }, supportingText = {
            if(showError) Text(LocalContext.current.getString(R.string.invalid_input))
        }, isError = showError)
}

@Preview
@Composable
fun PreviewValidPassword() {
    PasswordTextField(pwd = "password", showError = false) { }
}

@Preview
@Composable
fun PreviewInvalidPassword() {
    PasswordTextField(pwd = "pass", showError = true) { }
}