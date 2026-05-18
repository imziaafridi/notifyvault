//package com.ziaafridi.notifyvault.onboarding
//
//import android.content.Intent
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.net.toUri
//import com.ziaafridi.notifyvault.R
//
//@Composable
//fun TermsAndConditionsScreen(
//    onAccept: () -> Unit
//) {
//    val context = LocalContext.current
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp)
//            .verticalScroll(rememberScrollState()),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Spacer(modifier = Modifier.height(60.dp))
//
//        // Title
//        Text(
//            text = context.getString(R.string.onboarding_terms_title),
//            fontSize = 28.sp,
//            fontWeight = FontWeight.Bold,
//            color = MaterialTheme.colorScheme.primary,
//            textAlign = TextAlign.Center
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Description
//        Text(
//            text = context.getString(R.string.onboarding_terms_description),
//            fontSize = 16.sp,
//            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
//            textAlign = TextAlign.Center,
//            lineHeight = 24.sp
//        )
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        // Terms and Privacy Links
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
//            )
//        ) {
//            Column(
//                modifier = Modifier.padding(20.dp)
//            ) {
//                TextButton(
//                    onClick = {
//                        val intent = Intent(Intent.ACTION_VIEW,
//                            context.getString(R.string.terms_url).toUri())
//                        context.startActivity(intent)
//                    },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(
//                        text = context.getString(R.string.onboarding_terms_service),
//                        fontSize = 16.sp,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//
//                HorizontalDivider(
//                    modifier = Modifier.padding(vertical = 8.dp),
//                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
//                )
//
//                TextButton(
//                    onClick = {
//                        val intent = Intent(Intent.ACTION_VIEW,
//                            context.getString(R.string.privacy_url).toUri())
//                        context.startActivity(intent)
//                    },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(
//                        text = context.getString(R.string.onboarding_privacy_policy),
//                        fontSize = 16.sp,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.weight(1f))
//
//        // Accept Button
//        Button(
//            onClick = onAccept,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.primary
//            ),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Text(
//                text = context.getString(R.string.onboarding_accept_continue),
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Navigation dots
//        NavigationDots(currentStep = 0)
//
//        Spacer(modifier = Modifier.height(24.dp))
//    }
//}