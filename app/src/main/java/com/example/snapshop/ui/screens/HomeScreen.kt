package com.example.snapshop.ui.screens


import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.snapshop.R
import com.example.snapshop.data.model.Product
import com.example.snapshop.ui.navigation.Destinations
import com.example.snapshop.ui.viewmodels.HomeScreenViewModel


enum class SortOrder {
    NONE,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    RATING
}



@Composable
fun NewUserScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF7043), // Orange
                            Color(0xFFFC9673), // Light Red
                            Color.White
                        )
                    )
                )
        ) {
            // Card in the Middle
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(500.dp)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Welcome Image
                    Image(
                        painter = painterResource(id = R.drawable.welcome),
                        contentDescription = "Welcome Image",
                        modifier = Modifier
                            .size(250.dp)
                            .background(Color(0xFFFFFFFF), shape = RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Welcome Text
                    Text(
                        text = "Welcome to SnapShop!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722)
                        ),
                        textAlign = TextAlign.Center
                    )

                    // Subtitle Text
                    Text(
                        text = "Discover the best prices, compare deals, and save more on your shopping journey!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Get Started Button
                    Button(
                        onClick = { navController.navigate(Destinations.PROFILE) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun HomeScreen(navController: NavController,viewModel: HomeScreenViewModel = viewModel()) {
    // Collect state flows to trigger recomposition on state changes
    val isLoading by viewModel.isLoading.collectAsState()
    val isNewUser by viewModel.isNewUser.collectAsState()

    // Trigger re-fetch of transactions or user status when returning to this screen
    LaunchedEffect(Unit) {
        viewModel.checkUserStatus() // Ensures the latest user status is fetched
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                // Show a loading indicator while checking user status
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            isNewUser -> {
                // Navigate to the New User screen if it's a new user
                NewUserScreen(navController = navController)
            }

            else -> {
                // Navigate to the Returning User screen if it's a returning user
                ReturningUserScreen(navController = navController)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturningUserScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cartItemCount by viewModel.cartItemCount.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState() // Collect sort order state

    var showSortDialog by remember { mutableStateOf(false) } // State for showing sort dialog
    var isSearching by remember { mutableStateOf(false) } // Track if searching
    var searchHistory = remember { mutableStateListOf<String>() } // Track search history

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar with Search
        TopAppBar(
            title = {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.setSearchQuery(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    placeholder = { Text("Search products...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Search // Set IME action to Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                // Add to search history if not already present
                                if (!searchHistory.contains(searchQuery)) {
                                    if (searchHistory.size >= 5) {
                                        searchHistory.removeAt(0) // Remove oldest if more than 5
                                    }
                                    searchHistory.add(searchQuery)
                                }
                                viewModel.fetchProducts() // Fetch products when search is triggered
                                isSearching = true // Set searching state
                            }
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color.White
                    )
                )
            },
            actions = {
                IconButton(onClick = { navController.navigate(Destinations.CART) }) {
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge { Text(cartItemCount.toString()) }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart",tint = Color.White)
                    }
                }
                IconButton(onClick = { showSortDialog = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort",tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFFC825A)
            )
        )

        // Categories Section
        Text(
            "Categories",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color(0xFFE35B30)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryCard(
                    name = "All",
                    icon = Icons.Default.Apps,
                    isSelected = selectedCategory == null
                ) {
                    viewModel.setSelectedCategory(null)
                }
            }

            itemsIndexed(categories) { _, category ->
                CategoryCard(
                    name = category,
                    icon = when (category.toLowerCase()) {
                        "electronics" -> Icons.Default.PhoneAndroid
                        "clothing" -> Icons.Default.Checkroom
                        "jewelery" -> Icons.Default.Diamond
                        else -> Icons.Default.Category
                    },
                    isSelected = selectedCategory == category
                ) {
                    viewModel.setSelectedCategory(category)
                }
            }
        }

        // Product Grid
        ProductsGrid(
            viewModel = viewModel,
            navController = navController,
            selectedCategory = selectedCategory // Pass selectedCategory to trigger recomposition
        )
    }

    // Sort Dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort Options") },
            text = {
                Column {
                    SortOption("Price: Low to High") {
                        viewModel.setSortOrder(SortOrder.PRICE_LOW_TO_HIGH)
                        viewModel.fetchProducts() // Ensure products are fetched after sorting
                        showSortDialog = false
                    }
                    SortOption("Price: High to Low") {
                        viewModel.setSortOrder(SortOrder.PRICE_HIGH_TO_LOW)
                        viewModel.fetchProducts() // Ensure products are fetched after sorting
                        showSortDialog = false
                    }
                    SortOption("Rating") {
                        viewModel.setSortOrder(SortOrder.RATING)
                        viewModel.fetchProducts() // Ensure products are fetched after sorting
                        showSortDialog = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SortOption(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun ProductsGrid(
    viewModel: HomeScreenViewModel,
    navController: NavController,
    selectedCategory: String? // Accept selectedCategory to trigger recomposition
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products.size) { index ->
            val product = products[index]
            ProductCard(
                product = product,
                onAddToCart = { viewModel.addToCart(product) },
                onAddToCompare = { viewModel.addToCompare(product) },
                onProductClick = { 
                    navController.navigate("product/${product.id}") // Pass product ID
                }
            )
        }
    }
}


@Composable
fun CategoryCard(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(75.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFFE35B30)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = name,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected)
                    Color.White
                else
                    Color(0xFFFC825A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                name,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (isSelected) Color.White else Color(0xFFFC825A)
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit,
    onAddToCompare: () -> Unit,
    onProductClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp) // Adjusted height for better layout
            .clickable { onProductClick() }
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White) // Updated card background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Product Image
            AsyncImage(
                model = product.image,
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp) // Decreased image size
                    .clip(MaterialTheme.shapes.small),

            )

            Spacer(modifier = Modifier.height(8.dp))

            // Product Title
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium, // Decreased text size
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Price
            Text(
                text = "$${product.price}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFC825A) // Updated price color
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Buttons stacked vertically
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp) // Added padding for spacing
                        .height(35.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE35B30) // Updated button color
                    )
                ) {
                    Text("Cart", color = Color.White) // Updated button text color
                }
                Button(
                    onClick = onAddToCompare,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFC825A) // Updated button color
                    )
                ) {
                    Text("Compare", color = Color.White) // Updated button text color
                }
            }
        }
    }
}

@Composable
fun ErrorView(error: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("An error occurred")
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No products found")
    }
}
