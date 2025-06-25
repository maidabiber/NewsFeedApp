package etf.ri.rma.newsfeedapp.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterScreen(navController: NavController, odabraniOpsegDatuma: Pair<String, String>?, onCategoryChanged: (String) -> Unit, onDateRangeChanged: (Pair<String, String>?) -> Unit, nezeljeneRijeci: List<String>, onUnwantedWordsChanged: (List<String>) -> Unit, kategorijaKojuSmoIzabrali: String) {



    val dugmici = listOf(
        Pair("Sve", "filter_chip_all"),
        Pair("Politika", "filter_chip_pol"),
        Pair("Sport", "filter_chip_spo"),
        Pair("Umjetnost", "filter_chip_art"),
        Pair("Nauka", "filter_chip_sci"),
        Pair("Tehnologija", "filter_chip_tech")
    )
    var kategorijaKojaJeTrenutnoAktivna by rememberSaveable { mutableStateOf(dugmici.indexOfFirst { it.first == kategorijaKojuSmoIzabrali }.coerceAtLeast(0)) }


    var unesenaNepozeljnaRijec by rememberSaveable { mutableStateOf("") }
    var pocetakOdabranogDatuma by remember { mutableStateOf<Long?>(null) }
    var krajOdabranogDatuma by remember { mutableStateOf<Long?>(null) }

    val stanjeNezeljenihRijeci = remember { mutableStateListOf<String>().apply { addAll(nezeljeneRijeci) } }
    val formatDatumaZaPrikaz = remember {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf
    }

    var prikaziOdabirDatuma by remember { mutableStateOf(false) }
    val stanjeOdabiraDatuma = rememberDateRangePickerState()

    LaunchedEffect(odabraniOpsegDatuma) {
        if (odabraniOpsegDatuma != null) {
            val pocetak = formatDatumaZaPrikaz.parse(odabraniOpsegDatuma.first)?.time
            val kraj = formatDatumaZaPrikaz.parse(odabraniOpsegDatuma.second)?.time
            if (kraj != null && pocetak != null ) {
                stanjeOdabiraDatuma.setSelection(pocetak, kraj)
            }
        }
    }

    val dateRangeText = remember(stanjeOdabiraDatuma.selectedStartDateMillis, stanjeOdabiraDatuma.selectedEndDateMillis) {
        if (stanjeOdabiraDatuma.selectedStartDateMillis != null && stanjeOdabiraDatuma.selectedEndDateMillis != null) {
            formatDatumaZaPrikaz.format(Date(stanjeOdabiraDatuma.selectedStartDateMillis!!)) to formatDatumaZaPrikaz.format(Date(stanjeOdabiraDatuma.selectedEndDateMillis!!))
        } else {
            null }
    }


    val odabranaKategorijaRezultat = dugmici.getOrNull(kategorijaKojaJeTrenutnoAktivna)?.first ?: "Sve"
    val rezultatRasponaDatuma = dateRangeText
    val finalnaListaNezeljenihRijeci = stanjeNezeljenihRijeci.toList()

    val kontekst = LocalContext.current
    Column(modifier = Modifier.padding(20.dp).fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(15.dp))

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            dugmici.forEachIndexed { index, (name, tag) ->
                val vrijednost = kategorijaKojaJeTrenutnoAktivna == index
                FilterChip(

                    selected = kategorijaKojaJeTrenutnoAktivna == index,
                    onClick = { kategorijaKojaJeTrenutnoAktivna = index },
                    label = { Text(name) },
                    modifier = Modifier.testTag(tag),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(90, 90, 160),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFE6E6FA),
                        labelColor = Color.Black

                    )
                )
            }
        }
        Spacer( Modifier.height(32.dp))

        Text(text="Opseg datuma", fontSize = 22.sp, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {

            Card(
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.weight(2f).testTag("filter_daterange_display"),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE6E6FA)  )

            ) {
                Text(
                    text = if (dateRangeText != null) "${dateRangeText.first};${dateRangeText.second}" else "Odaberite opseg datuma",
                    color = Color.DarkGray,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { prikaziOdabirDatuma = true },
                modifier = Modifier.testTag("filter_daterange_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(70, 70, 150),
                    contentColor = Color.White
                )

            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Odaberi datume")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Odaberi")
            }
        }
        Spacer(Modifier.height(32.dp))
        val ljubicasta= Color(0xFFE6E6FA)
        Text("Nepoželjne riječi", fontSize = 22.sp, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, ) {
            TextField(


                value = unesenaNepozeljnaRijec,
                onValueChange = { unesenaNepozeljnaRijec = it },
                modifier = Modifier.width(226.dp).testTag("filter_unwanted_input") .shadow(4.dp, shape = RoundedCornerShape(8.dp)).background(ljubicasta, shape = RoundedCornerShape(8.dp)),
                placeholder = {
                    Text(text="Unesite riječ", color = Color.DarkGray)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ljubicasta, unfocusedContainerColor = ljubicasta,
                    disabledContainerColor = ljubicasta, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent, cursorColor = Color.Black
                ),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(8.dp),)
            Spacer(modifier = Modifier.width(34.dp))
            Button(
                onClick = {
                    val rijec = unesenaNepozeljnaRijec.trim()

                    val poruka = if (rijec.isEmpty()) {
                        "Unesite riječ!"
                    } else if (stanjeNezeljenihRijeci.any { it.equals(rijec, ignoreCase = true) }) {
                        "Nije moguće unijeti istu riječ više puta!"
                    } else {
                        null
                    }


                    if (poruka != null) {
                        Toast.makeText(kontekst, poruka, Toast.LENGTH_SHORT).show()
                    } else {
                        stanjeNezeljenihRijeci.add(rijec)
                        unesenaNepozeljnaRijec = ""
                    }
                },   colors = ButtonDefaults.buttonColors(
                    containerColor = Color(70, 70, 150),
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("filter_unwanted_add_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj riječ", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(7.dp))
                Text("Dodaj")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.testTag("filter_unwanted_list")
            .verticalScroll(rememberScrollState())
        ) {
            stanjeNezeljenihRijeci.forEach { rijec ->
                Text(text = rijec)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onCategoryChanged(odabranaKategorijaRezultat)
                onDateRangeChanged(rezultatRasponaDatuma)
                onUnwantedWordsChanged(finalnaListaNezeljenihRijeci)

                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(70, 70, 150),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().testTag("filter_apply_button")
        ) {
            Text("Primijeni filtere")
        }
        val pocetak = stanjeOdabiraDatuma.selectedStartDateMillis
        val kraj = stanjeOdabiraDatuma.selectedEndDateMillis
        if (prikaziOdabirDatuma) {
            AlertDialog(onDismissRequest = { prikaziOdabirDatuma = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (pocetak != null && kraj != null) {
                                pocetakOdabranogDatuma = pocetak
                                krajOdabranogDatuma = kraj
                                prikaziOdabirDatuma = false
                            }
                        }
                    ) {
                        Text("Potvrdi opseg datuma")
                    }
                },
                dismissButton = { TextButton(onClick = { prikaziOdabirDatuma = false }) {
                    Text("Odustani") }
                },
                text = { DateRangePicker(state = stanjeOdabiraDatuma) }
            )
        }
    }
}