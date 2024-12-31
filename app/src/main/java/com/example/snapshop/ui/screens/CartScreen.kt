package com.example.snapshop.ui.screens


import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.snapshop.data.model.CartItem
import com.example.snapshop.ui.viewmodels.CartViewModel
import com.example.snapshop.utils.BiometricHelper
import com.example.snapshop.utils.LocationHelper


@Composable
fun CartScreen(
    navController: NavController,
    onOrderPlaced: () -> Unit,
    viewModel: CartViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val biometricHelper = remember { BiometricHelper(activity) }
    val locationHelper = remember { LocationHelper(context) }
    var address by remember { mutableStateOf("") }
    var isLoadingAddress by remember { mutableStateOf(true) }
    var showAuthError by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf("") }
    var showLocationDialog by remember { mutableStateOf(false) }

    fun proceedWithOrder() {
        if (address.isBlank()) {
            authErrorMessage = "Please enter a delivery address"
            showAuthError = true
            return
        }

        if (biometricHelper.isAuthenticationAvailable(context)) {
            biometricHelper.showAuthenticationPrompt(
                onSuccess = {
                    viewModel.placeOrder(address) {
                        onOrderPlaced()
                    }
                },
                onError = { errorCode, errorString ->
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && 
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        authErrorMessage = when (errorCode) {
                            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> 
                                "Please set up a screen lock (PIN, pattern, or password) to proceed"
                            else -> errorString.toString()
                        }
                        showAuthError = true
                    }
                },
                onNotAvailable = {
                    // Proceed without authentication if not available
                    viewModel.placeOrder(address) {
                        onOrderPlaced()
                    }
                }
            )
        } else {
            // Device doesn't support biometric or device credentials
            viewModel.placeOrder(address) {
                onOrderPlaced()
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoadingAddress = true
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isLocationEnabled) {
                showLocationDialog = true
            } else {
                locationHelper.getCurrentAddress()?.let {
                    address = it
                }
            }
        } catch (e: Exception) {
            // Handle location error silently - user can still enter address manually
        } finally {
            isLoadingAddress = false
        }
    }

    val cartItems by viewModel.cartItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val total by viewModel.total.collectAsState()

    if (showAuthError) {
        AlertDialog(
            onDismissRequest = { showAuthError = false },
            title = { Text("Authentication Failed") },
            text = { Text(authErrorMessage) },
            confirmButton = {
                TextButton(onClick = { showAuthError = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Location Services Disabled") },
            text = { Text("Please enable location services to automatically fetch your delivery address.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationDialog = false
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Shopping Cart",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Your cart is empty")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems) { item ->
                    CartItemCard(
                        item = item,
                        onIncrement = { viewModel.updateQuantity(item.productId, true) },
                        onDecrement = { viewModel.updateQuantity(item.productId, false) },
                        onRemove = { viewModel.removeItem(item.productId) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total and Checkout section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total:",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "$${String.format("%.2f", total)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Delivery Address") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        trailingIcon = if (isLoadingAddress) {
                            { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                        } else null
                    )

                    Button(
                        onClick = { proceedWithOrder() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE35B30)),
                        enabled = cartItems.isNotEmpty()
                    ) {
                        Text("Place Order", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.image,
                contentDescription = item.title,
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2
                )
                Text(
                    text = "$${item.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDecrement) {
                        Text("-")
                    }
                    Text(
                        text = "${item.quantity}",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = onIncrement) {
                        Text("+")
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color.Red
                    )
                }
            }
        }
    }
} 