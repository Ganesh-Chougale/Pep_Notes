package com.horizone.pep_notes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun LabelChip(
    label: String,
    isSelected: Boolean = false,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedIndicatorColor: Color = Color(0xFFFFD700) // Golden color for the indicator
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scaleAnimation"
    )
    
    // Golden circular completion progress for the indicator
    val indicatorProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "indicatorProgress"
    )
    
    // Animate the colors for smooth transitions
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSelected) selectedContainerColor else containerColor,
        animationSpec = tween(durationMillis = 300),
        label = "containerColorAnimation"
    )
    
    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) selectedContentColor else contentColor,
        animationSpec = tween(durationMillis = 300),
        label = "contentColorAnimation"
    )
    
    // Calculate luminance for theme detection
    val primaryLuminance = with(MaterialTheme.colorScheme.primary) {
        0.299f * red + 0.587f * green + 0.114f * blue
    }
    val isLightTheme = primaryLuminance > 0.5f

    // Opposite-theme colors for icons and cancel button, based on onSurface
    val iconOppositeColorTarget = MaterialTheme.colorScheme.onSurface

    val iconTint by animateColorAsState(
        targetValue = iconOppositeColorTarget,
        animationSpec = tween(durationMillis = 300),
        label = "iconTintAnimation"
    )

    val cancelButtonColor by animateColorAsState(
        targetValue = iconOppositeColorTarget,
        animationSpec = tween(durationMillis = 300),
        label = "cancelColorAnimation"
    )
    
    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp)),
        color = animatedContainerColor,
        contentColor = animatedContentColor,
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = onRemove != null) { onRemove?.invoke() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Circular indicator with golden glow effect
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawBehind {
                        if (indicatorProgress > 0f) {
                            val strokeWidth = size.minDimension * 0.18f
                            val radius = size.minDimension / 2f - strokeWidth / 2f
                            drawCircle(
                                color = selectedIndicatorColor,
                                radius = radius * indicatorProgress.coerceIn(0f, 1f),
                                style = Stroke(width = strokeWidth),
                                alpha = 0.9f
                            )
                        }
                    }
                    .clip(CircleShape)
                    .background(if (isSelected) selectedIndicatorColor else Color.Transparent)
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = if (isSelected) selectedIndicatorColor 
                               else contentColor.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Selected",
                        modifier = Modifier.size(14.dp),
                        tint = iconTint
                    )
                }
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = animatedContentColor
            )
            
            if (onRemove != null) {
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onRemove() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove label",
                            modifier = Modifier.size(18.dp),
                            tint = cancelButtonColor
                        )
                    }
                }
            }
        }
    }
}

private fun Color.darken(factor: Float): Color {
    return copy(red = (red * (1 - factor)).coerceIn(0f, 1f),
               green = (green * (1 - factor)).coerceIn(0f, 1f),
               blue = (blue * (1 - factor)).coerceIn(0f, 1f))
}

private fun Color.lighten(factor: Float): Color {
    return copy(red = (red + (1 - red) * factor).coerceIn(0f, 1f),
               green = (green + (1 - green) * factor).coerceIn(0f, 1f),
               blue = (blue + (1 - blue) * factor).coerceIn(0f, 1f))
}
