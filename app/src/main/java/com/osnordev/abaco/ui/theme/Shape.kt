package com.osnordev.abaco.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // Small components: chips, text fields
    small = RoundedCornerShape(8.dp),
    // Medium components: cards, dialogs
    medium = RoundedCornerShape(12.dp),
    // Large components: bottom sheets, nav drawers
    large = RoundedCornerShape(16.dp),
    // Extra large: full-screen sheets
    extraLarge = RoundedCornerShape(28.dp)
)
