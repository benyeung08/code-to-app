package com.webtoapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.webtoapp.core.i18n.RandomAppNameGenerator
import com.webtoapp.core.i18n.Strings

/**
 * App-name input styled as an outlined field so it matches the website-URL field
 * on the same screen (floating label on the stroke, same corner radius).
 */
@Composable
fun AppNameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    PremiumTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(Strings.labelAppName) },
        placeholder = { Text(placeholder ?: Strings.inputAppName) },
        leadingIcon = { Icon(Icons.Outlined.Badge, null) },
        trailingIcon = {
            IconButton(
                onClick = { onValueChange(RandomAppNameGenerator.generate()) }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Casino,
                    contentDescription = Strings.randomNameTooltip,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = imeAction)
    )
}

@Composable
fun AppNameTextFieldSimple(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null
) {
    PremiumTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(Strings.labelAppName) },
        placeholder = { Text(placeholder ?: Strings.inputAppName) },
        leadingIcon = { Icon(Icons.Outlined.Badge, null) },
        trailingIcon = {
            IconButton(
                onClick = { onValueChange(RandomAppNameGenerator.generate()) }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Casino,
                    contentDescription = Strings.randomNameTooltip,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        singleLine = true
    )
}
