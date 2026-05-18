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
fun PrivacyScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6A4C93), // Purple at top
                        Color(0xFF4A2C5A)  // Darker purple at bottom
                    )
                )
            )
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        // Privacy illustration placeholder
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(100.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔒",
                fontSize = 80.sp
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Title
        Text(
            text = context.getString(R.string.onboarding_privacy_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Description
        Text(
            text = context.getString(R.string.onboarding_privacy_description),
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
                .padding(bottom = 40.dp)
        )

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = context.getString(R.string.onboarding_media_continue),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
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
                            if (index == 5) Color.White else Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}