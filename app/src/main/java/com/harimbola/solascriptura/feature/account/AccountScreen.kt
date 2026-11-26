package com.harimbola.solascriptura.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.harimbola.solascriptura.core.designsystem.LocalAppDimensions
import com.harimbola.solascriptura.core.designsystem.SolaSurfaceCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dims = LocalAppDimensions.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("account_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = dims.maxContentWidth)
                    .fillMaxSize(),
                contentPadding = PaddingValues(dims.screenPadding),
                verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
            ) {
                item {
                    SolaSurfaceCard(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Text(
                            text = stringResource(R.string.account_optional_notice),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!state.isAppwriteConfigured) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.account_unconfigured_notice),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                state.statusMessage?.let { msg ->
                    item {
                        SolaSurfaceCard(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                state.error?.let { err ->
                    item {
                        SolaSurfaceCard(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = err.detailMessage?.takeIf { it.isNotBlank() }?.let {
                                    "${stringResource(err.messageResId)} ($it)"
                                } ?: stringResource(err.messageResId),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                if (state.isBusy) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                val user = state.user
                if (user == null) {
                    item {
                        SolaSurfaceCard {
                            TabRow(selectedTabIndex = selectedTab) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text(stringResource(R.string.account_sign_in)) }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text(stringResource(R.string.account_create)) }
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (selectedTab == 1) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Nom") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("account_name_input")
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("E-mail") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("account_email_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Mot de passe") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("account_password_input")
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (selectedTab == 0) {
                                        viewModel.signIn(email, password)
                                    } else {
                                        viewModel.signUp(name, email, password)
                                    }
                                },
                                enabled = !state.isBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("account_submit_button")
                            ) {
                                Text(
                                    if (selectedTab == 0) {
                                        stringResource(R.string.account_sign_in)
                                    } else {
                                        stringResource(R.string.account_create)
                                    }
                                )
                            }
                            if (selectedTab == 0 && email.isNotBlank()) {
                                TextButton(
                                    onClick = { viewModel.sendRecovery(email) },
                                    modifier = Modifier.testTag("account_recovery_button")
                                ) {
                                    Text("Mot de passe oublié ?")
                                }
                            }
                        }
                    }
                } else {
                    item {
                        SolaSurfaceCard {
                            Text(user.name, style = MaterialTheme.typography.titleLarge)
                            Text(
                                user.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val lastSyncLabel = if (state.lastSyncTimestamp > 0L) {
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    .format(Date(state.lastSyncTimestamp))
                            } else {
                                "Jamais"
                            }
                            Text(
                                text = "En file d'attente : ${state.pendingQueueCount} · Dernière synchro : $lastSyncLabel",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.syncNow() },
                                    enabled = !state.isBusy,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("account_sync_now_button")
                                ) {
                                    Icon(Icons.Rounded.CloudSync, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.account_sync_now))
                                }
                                OutlinedButton(
                                    onClick = { viewModel.transferLocalProgress() },
                                    enabled = !state.isBusy,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("account_transfer_button")
                                ) {
                                    Icon(Icons.Rounded.CloudUpload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.account_transfer_local))
                                }
                                OutlinedButton(
                                    onClick = { viewModel.signOut() },
                                    enabled = !state.isBusy,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("account_sign_out_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.account_sign_out))
                                }
                            }
                        }
                    }

                    if (state.remoteCatalog.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.home_new_downloads_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(state.remoteCatalog, key = { "${it.studyId}_${it.locale}" }) { item ->
                            SolaSurfaceCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            item.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.downloadCatalogItem(item) },
                                        modifier = Modifier.testTag("account_download_${item.studyId}")
                                    ) {
                                        Icon(Icons.Rounded.CloudDownload, contentDescription = "Télécharger")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
