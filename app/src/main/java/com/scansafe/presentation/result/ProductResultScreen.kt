package com.scansafe.presentation.result

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.scansafe.domain.model.*
import com.scansafe.presentation.components.ErrorState
import com.scansafe.presentation.components.HealthScoreRing
import com.scansafe.presentation.components.IngredientChip
import com.scansafe.presentation.components.SkeletonLoader
import com.scansafe.ui.theme.*

private fun getGradeFromScore(score: Int): String {
    return when {
        score >= 9 -> "A"
        score >= 7 -> "B"
        score >= 5 -> "C"
        score >= 3 -> "D"
        else -> "E"
    }
}

@Composable
fun ProductResultScreen(
    barcode: String,
    onNavigateBack: () -> Unit,
    viewModel: ProductResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(barcode) {
        viewModel.loadProduct(barcode)
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ProductUiState.Loading -> ProductResultSkeleton(onBack = onNavigateBack)
                is ProductUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadProduct(barcode) },
                    onBack = onNavigateBack
                )
                is ProductUiState.Success -> ProductResultContent(
                    product = state.product,
                    isFavourite = state.isFavourite,
                    onBack = onNavigateBack,
                    onToggleFavourite = { viewModel.toggleFavourite(barcode) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductResultContent(
    product: Product,
    isFavourite: Boolean,
    onBack: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Sticky Bottom Action Bar containing share and favorites toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share button
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out this food product: ${product.name} on ScanSafe! It has a health score of ${product.aiAnalysis?.healthScore ?: 0}/10."
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Product"))
                        },
                        modifier = Modifier.height(48.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Add to Favorites Pill button
                    val favoriteTransitionScale by animateFloatAsState(
                        targetValue = if (isFavourite) 1.08f else 1.0f,
                        label = "fav_scale"
                    )

                    Button(
                        onClick = onToggleFavourite,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFavourite) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentColor = if (isFavourite) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFavourite) HealthScoreScale.E else LocalContentColor.current,
                                modifier = Modifier
                                    .size(20.dp)
                                    .scale(favoriteTransitionScale)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFavourite) "Saved to Favorites" else "Add to Favorites",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Product header
            item { ProductHeader(product) }

            product.aiAnalysis?.let { ai ->
                // 2. Health score gauge & ring card
                item { HealthScoreCard(ai.healthScore, ai.scoreLabel) }

                // 3. AI analysis section (Expandable card with animateContentSize)
                item { ExpandableAiAnalysisCard(summary = ai.summary, recommendation = ai.recommendation) }

                // 4. Allergen highlights
                if (ai.allergensDetected.isNotEmpty()) {
                    item { AllergenCard(ai.allergensDetected) }
                }

                // 5. Ingredients (grouped card containing categorised IngredientChips)
                item {
                    IngredientsGroupCard(
                        harmful = ai.harmfulIngredients,
                        safe = ai.safeIngredients,
                        additives = ai.additives
                    )
                }
            }

            // 6. Nutrition facts table
            item { NutritionCard(product.nutrition) }

            // Bottom Spacer
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ProductHeader(product: Product) {
    val score = product.aiAnalysis?.healthScore ?: 5
    val scoreColor = HealthScoreScale.getColorForScore(score * 10)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Frosted-looking Image container
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("📦", fontSize = 36.sp)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!product.brand.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = product.brand,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                if (!product.quantity.isNullOrEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = product.quantity,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                if (!product.nutriScore.isNullOrEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    val nc = when (product.nutriScore.uppercase()) {
                        "A" -> NutriA
                        "B" -> NutriB
                        "C" -> NutriC
                        "D" -> NutriD
                        else -> NutriE
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(nc.copy(alpha = 0.12f))
                            .border(1.dp, nc.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(nc),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = product.nutriScore.uppercase(),
                                    fontSize = 8.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Nutri-Score ${product.nutriScore.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = nc,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthScoreCard(healthScore: Int, scoreLabel: String) {
    val scoreColor = HealthScoreScale.getColorForScore(healthScore * 10)
    val grade = getGradeFromScore(healthScore)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HealthScoreRing component integration
            HealthScoreRing(
                score = healthScore * 10,
                grade = grade,
                size = 96.dp,
                strokeWidth = 9.dp
            )

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Health Score",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scoreColor.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = scoreLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = scoreColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(14.dp))

                // Coloured health score scale bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(1, 2, 3).forEach { index ->
                        val active = when (index) {
                            1 -> healthScore in 1..3
                            2 -> healthScore in 4..6
                            else -> healthScore in 7..10
                        }
                        val col = when (index) {
                            1 -> HealthScoreScale.E
                            2 -> HealthScoreScale.C
                            else -> HealthScoreScale.A
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(if (active) col else col.copy(alpha = 0.15f))
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Avoid", style = MaterialTheme.typography.labelSmall, color = HealthScoreScale.E)
                    Text("Optimal", style = MaterialTheme.typography.labelSmall, color = HealthScoreScale.A)
                }
            }
        }
    }
}

@Composable
private fun ExpandableAiAnalysisCard(summary: String, recommendation: String) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, AiViolet.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize(tween(400, easing = FastOutSlowInEasing))
        ) {
            // Header row with Expand Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AiViolet, AiTeal)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Health Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Powered by Claude AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = AiViolet,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (summary.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            if (expanded && recommendation.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(Modifier.height(12.dp))

                val parts = recommendation.split("\n\n").filter { it.isNotBlank() }
                parts.forEachIndexed { index, part ->
                    when {
                        part.startsWith("Why to consume:") -> AiTile(
                            icon = "👍",
                            title = "Why to Consume",
                            body = part.removePrefix("Why to consume:").trim(),
                            accentColor = HealthScoreScale.A,
                            bgColor = HealthScoreScale.A.copy(alpha = 0.08f)
                        )
                        part.startsWith("Why NOT to consume:") -> {
                            val body = part.removePrefix("Why NOT to consume:").trim()
                            if (body.isNotBlank() && body != "No major health hazards detected.") {
                                AiTile(
                                    icon = "👎",
                                    title = "Why NOT to Consume",
                                    body = body,
                                    accentColor = HealthScoreScale.E,
                                    bgColor = HealthScoreScale.E.copy(alpha = 0.08f)
                                )
                            }
                        }
                        part.startsWith("Amount to consume:") -> AiTile(
                            icon = "⚖️",
                            title = "Amount Guidelines",
                            body = part.removePrefix("Amount to consume:").trim(),
                            accentColor = AiTeal,
                            bgColor = AiTeal.copy(alpha = 0.08f)
                        )
                        part.isNotBlank() -> AiTile(
                            icon = "💡",
                            title = "Note",
                            body = part.trim(),
                            accentColor = HealthScoreScale.C,
                            bgColor = HealthScoreScale.C.copy(alpha = 0.08f)
                        )
                    }
                    if (index < parts.size - 1) Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun AiTile(
    icon: String,
    title: String,
    body: String,
    accentColor: Color,
    bgColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            if (body.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun AllergenCard(allergens: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = HealthScoreScale.E.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, HealthScoreScale.E.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = HealthScoreScale.E,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 1.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Allergens Detected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HealthScoreScale.E
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = allergens.joinToString(separator = ", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IngredientsGroupCard(
    harmful: List<HarmfulIngredient>,
    safe: List<String>,
    additives: List<Additive>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Card Title Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ingredients Specifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            // 1. Harmful ingredients if any
            if (harmful.isNotEmpty()) {
                Text(
                    text = "Attention Required",
                    style = MaterialTheme.typography.labelLarge,
                    color = HealthScoreScale.E,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    harmful.forEach { item ->
                        IngredientChip(name = item.name, classification = "allergen")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 2. Additives if any
            if (additives.isNotEmpty()) {
                Text(
                    text = "Additives & Preservatives",
                    style = MaterialTheme.typography.labelLarge,
                    color = HealthScoreScale.C,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    additives.forEach { item ->
                        val classification = when (item.risk.lowercase()) {
                            "safe" -> "good"
                            "moderate" -> "additive"
                            else -> "allergen"
                        }
                        IngredientChip(name = "${item.code}: ${item.name}", classification = classification)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 3. Safe ingredients
            if (safe.isNotEmpty()) {
                Text(
                    text = "Clean Ingredients",
                    style = MaterialTheme.typography.labelLarge,
                    color = HealthScoreScale.A,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    safe.forEach { item ->
                        IngredientChip(name = item, classification = "good")
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionCard(n: NutritionInfo) {
    val empty = n.calories == 0.0 && n.protein == 0.0 && n.carbs == 0.0 &&
            n.fat == 0.0 && n.sugar == 0.0 && n.fiber == 0.0 && n.sodium == 0.0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Nutrition Facts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (!empty) {
                    Text(
                        text = "Per 100g",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (empty) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ℹ️", fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Nutrition facts not available for this product.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Column
            }

            // Energy Calorie Highlight Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
                            )
                        )
                    )
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Calories",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${n.calories.toInt()} kcal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${((n.calories / 2000.0) * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Daily Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Nutrition list mapping
            val rows = listOf(
                Triple("🥩 Protein", "${n.protein}g", n.protein / 50.0),
                Triple("🍞 Carbs", "${n.carbs}g", n.carbs / 300.0),
                Triple("🧈 Fat", "${n.fat}g", n.fat / 65.0),
                Triple("🍬 Sugar", "${n.sugar}g", n.sugar / 50.0),
                Triple("🌾 Fiber", "${n.fiber}g", n.fiber / 25.0),
                Triple("🧂 Sodium", "${n.sodium}mg", n.sodium / 2300.0)
            )

            rows.forEachIndexed { index, row ->
                val ratio = row.third.coerceIn(0.0, 1.0).toFloat()
                val barColor = when {
                    ratio < 0.33f -> HealthScoreScale.A
                    ratio < 0.66f -> HealthScoreScale.C
                    else -> HealthScoreScale.E
                }
                val animatedProgress by animateFloatAsState(
                    targetValue = ratio,
                    animationSpec = tween(1000, easing = EaseOutCubic),
                    label = "nutri_progress"
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row.first,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = row.second,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                }
                if (index < rows.size - 1) Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ProductResultSkeleton(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Default.ArrowBack, "Back")
        }
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonLoader(modifier = Modifier.fillMaxWidth().height(130.dp))
            SkeletonLoader(modifier = Modifier.fillMaxWidth().height(100.dp))
            SkeletonLoader(modifier = Modifier.fillMaxWidth().height(160.dp))
            SkeletonLoader(modifier = Modifier.fillMaxWidth().height(120.dp))
        }
    }
}
