package com.horizone.pep_notes.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.foundation.text.ClickableText

// -------- Constants ----------
const val ABOUT_APP_NAME = "Pep Notes"
const val ABOUT_APP_VERSION = "1.0"
const val ABOUT_APP_DESCRIPTION =
    "Pep Notes is a clean, distraction-free note-taking app."
const val ABOUT_DEV_NAME = "Ganesh Chougale"
const val ABOUT_DEV_BIO = "Developer & Writer"
const val ABOUT_EMAIL = "gchougale32@gmail.com"
const val ABOUT_WEBSITE = "https://ganesh-chougale.github.io/"
// -------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(
    navController: NavHostController
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Us") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            InfoCard {
                Text("App Information", fontWeight = FontWeight.SemiBold)
                Text("App Name: $ABOUT_APP_NAME")
                Text("Version: $ABOUT_APP_VERSION")
                Text(
                    ABOUT_APP_DESCRIPTION,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            InfoCard {
                Text("Developer", fontWeight = FontWeight.SemiBold)
                Text(ABOUT_DEV_NAME)
                Text(
                    ABOUT_DEV_BIO,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ContactCard()
        }
    }
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun ContactCard() {
    val context = LocalContext.current

    val labelColor = MaterialTheme.colorScheme.onSurface
    val linkColor = MaterialTheme.colorScheme.onSurfaceVariant

    // EMAIL TEXT
    val emailAnnotated = buildAnnotatedString {
        append("Email: ")
        addStyle(SpanStyle(color = labelColor), 0, length)

        val start = length
        append(ABOUT_EMAIL)
        addStyle(
            SpanStyle(
                color = linkColor,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.SemiBold
            ),
            start,
            length
        )
        addStringAnnotation("EMAIL", ABOUT_EMAIL, start, length)
    }

    // WEBSITE TEXT
    val websiteAnnotated = buildAnnotatedString {
        append("Website: ")
        addStyle(SpanStyle(color = labelColor), 0, length)

        val start = length
        append(ABOUT_WEBSITE)
        addStyle(
            SpanStyle(
                color = linkColor,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.SemiBold
            ),
            start,
            length
        )
        addStringAnnotation("URL", ABOUT_WEBSITE, start, length)
    }

    InfoCard {

        Text("Contact & Links", fontWeight = FontWeight.SemiBold)

        // EMAIL ROW
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.MailOutline, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            ClickableText(
                text = emailAnnotated,
                onClick = { offset ->
                    emailAnnotated.getStringAnnotations("EMAIL", offset, offset)
                        .firstOrNull()?.let {
                            val intent = Intent(Intent.ACTION_SENDTO)
                            intent.data = Uri.parse("mailto:${it.item}")
                            context.startActivity(intent)
                        }
                }
            )
        }

        // WEBSITE ROW
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Language, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            ClickableText(
                text = websiteAnnotated,
                onClick = { offset ->
                    websiteAnnotated.getStringAnnotations("URL", offset, offset)
                        .firstOrNull()?.let {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(it.item)
                            context.startActivity(intent)
                        }
                }
            )
        }
    }
}
