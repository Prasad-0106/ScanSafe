package com.scansafe.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scansafe.ui.theme.HealthScoreScale

enum class PillButtonType {
    Primary,
    Secondary,
    Tonal
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    type: PillButtonType = PillButtonType.Primary,
    leadingIcon: @Composable (() -> Unit)? = null,
    isLoading: Boolean = false
) {
    val shape = CircleShape
    val colors = when (type) {
        PillButtonType.Primary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        )
        PillButtonType.Secondary -> ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        PillButtonType.Tonal -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (type == PillButtonType.Secondary) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .height(48.dp)
                .defaultMinSize(minWidth = 120.dp),
            enabled = enabled && !isLoading,
            shape = shape,
            colors = colors,
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.5.dp,
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
            ),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            ButtonContent(text = text, leadingIcon = leadingIcon, isLoading = isLoading)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier
                .height(48.dp)
                .defaultMinSize(minWidth = 120.dp),
            enabled = enabled && !isLoading,
            shape = shape,
            colors = colors,
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            ButtonContent(text = text, leadingIcon = leadingIcon, isLoading = isLoading)
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: @Composable (() -> Unit)?,
    isLoading: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = LocalContentColor.current,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun HealthScoreRing(
    score: Int,
    grade: String,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 10.dp
) {
    val targetSweep = (score.coerceIn(0, 100) / 100f) * 360f
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = targetSweep,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val scoreColor = HealthScoreScale.getColorForGrade(grade)
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw background track
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokeWidth.toPx())
            )

            // Draw filled arc
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = animatedProgress.value,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = grade.uppercase(),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                color = scoreColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun IconChip(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(12.dp)
    val baseModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Row(
        modifier = baseModifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), shape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
fun IngredientChip(
    name: String,
    classification: String, // good, neutral, additive, allergen
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (classification.lowercase()) {
        "good" -> Pair(Color(0xFFE8F5E9), Color(0xFF1B5E20))
        "neutral" -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        "additive" -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
        "allergen" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .background(backgroundColor, shape)
            .border(1.dp, textColor.copy(alpha = 0.12f), shape)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
