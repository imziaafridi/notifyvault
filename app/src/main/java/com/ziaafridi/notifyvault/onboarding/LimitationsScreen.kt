package com.ziaafridi.notifyvault.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ziaafridi.notifyvault.R

@Composable
fun LimitationsScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E5266), // Blue-gray at top
                        Color(0xFF1A3A4A)  // Darker blue-gray at bottom
                    )
                )
            )
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Title
        Text(
            text = context.getString(R.string.onboarding_limitations_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // First limitation
        LimitationCard(
            text = context.getString(R.string.onboarding_limitations_offline)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Second limitation
        LimitationCard(
            text = context.getString(R.string.onboarding_limitations_wifi)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Recommendation
        RecommendationCard(
            text = context.getString(R.string.onboarding_limitations_recommendation)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Finish Button
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = context.getString(R.string.onboarding_finish),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Navigation dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(7) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (index == 6) Color.White else Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LimitationCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.White,
            lineHeight = 24.sp,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
private fun RecommendationCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = Color.White,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Settings link placeholder
            Text(
                text = "Settings > Data and storage usage",
                fontSize = 14.sp,
                color = Color(0xFF81C784),
                fontWeight = FontWeight.Medium
            )
        }
    }
}