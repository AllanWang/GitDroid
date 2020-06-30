package ca.allanwang.gitdroid.views.compose

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.layout.Row
import androidx.ui.layout.Spacer
import androidx.ui.layout.padding
import androidx.ui.layout.size
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.RippleIndication
import androidx.ui.res.vectorResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import ca.allanwang.gitdroid.views.R

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun ProfileRow(iconRes: Int, text: String, chevronClick: (() -> Unit)? = null) {
//    MaterialTheme {
    Row(verticalGravity = Alignment.CenterVertically) {
        Image(
            asset = vectorResource(iconRes),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.caption
        )
        if (chevronClick != null) {
            Spacer(
                modifier = Modifier.weight(1f)
            )
            Image(
                asset = vectorResource(R.drawable.ic_chevron_right),
                modifier = Modifier.size(24.dp)
                    .clickable(
                        onClick = chevronClick,
                        indication = RippleIndication(bounded = false)
                    )
            )
        }
    }
//    }
}

@Preview
@Composable
fun PreviewGreeting() {
//    Greeting("Android")
    ProfileRow(iconRes = R.drawable.ic_branch, text = "Test") {}
}