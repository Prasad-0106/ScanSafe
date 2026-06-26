package com.scansafe.presentation.favourites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.scansafe.domain.model.Favourite
import com.scansafe.presentation.components.EmptyState
import com.scansafe.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    onProductClicked: (String) -> Unit,
    onNavigateToScanner: () -> Unit = {},
    viewModel: FavouritesViewModel = hiltViewModel()
) {
    val favourites by viewModel.favourites.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.syncFavourites()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (favourites.isEmpty()) {
            EmptyState(
                emoji = "❤️",
                title = "No Favorites Saved",
                subtitle = "Save your favorite healthy products to access them instantly.",
                actionText = "Start Scanning",
                onActionClick = onNavigateToScanner,
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 100.dp // Clear bottom bar space
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(favourites, key = { it.id }) { favourite ->
                    FavouriteCard(
                        favourite = favourite,
                        onClick = { onProductClicked(favourite.product.barcode) },
                        onRemove = { viewModel.removeFavourite(favourite.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavouriteCard(
    favourite: Favourite,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val score = favourite.product.aiAnalysis?.healthScore
    val scoreColor = when {
        score == null -> Color.Gray
        score >= 8 -> HealthScoreScale.A
        score >= 6 -> HealthScoreScale.B
        score >= 4 -> HealthScoreScale.C
        score >= 2 -> HealthScoreScale.D
        else -> HealthScoreScale.E
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                // Product Thumbnail box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (favourite.product.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = favourite.product.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛒", fontSize = 32.sp)
                        }
                    }

                    // Score Badge Overlay
                    if (score != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(scoreColor.copy(alpha = 0.9f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$score/10",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Name / Brand labels
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = favourite.product.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (favourite.product.brand.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = favourite.product.brand,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Floating Delete button top-left
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .padding(6.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from favorites",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
