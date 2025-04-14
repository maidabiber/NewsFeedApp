package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material3.Card

import androidx.compose.runtime.Composable

import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment

@Composable
fun MessageCard(nekaPoruka: String) {
    Card( modifier = Modifier.padding(6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(13.dp).fillMaxWidth(),

        ) {
            Text(nekaPoruka)
        }
    }
}