package com.transition.lensesdelivery.presentation.delivery_confirm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.transition.lensesdelivery.R
import com.transition.lensesdelivery.domain.model.QueueDetail
import com.transition.lensesdelivery.ui.theme.sourceCodeProFontFamily

@Composable
fun LineButton(label: String = "", isEnable: Boolean = false, onClick: () -> Unit) {
    FilledTonalButton(
        modifier = Modifier
            .width(130.dp)
            .height(90.dp),
        enabled = isEnable,
        colors = ButtonDefaults.buttonColors(
            Color(0xFF4EDFFF)
        ),
        shape = RoundedCornerShape(20.dp),
        onClick = { onClick() }) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 22.sp,
            fontFamily = sourceCodeProFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun LabButton(isEnable: Boolean = false, onClick: () -> Unit) {
    FilledTonalButton(
        modifier = Modifier
            .width(360.dp)
            .height(60.dp),
        enabled = isEnable,
        colors = ButtonDefaults.buttonColors(
            Color(0xFF35F68E)
        ),
        shape = RoundedCornerShape(20.dp),
        onClick = { onClick() }) {
        Text(
            text = "Laboratory",
            color = Color.White,
            fontSize = 32.sp,
            fontFamily = sourceCodeProFontFamily,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun BatteryInfo(percentage: Int = 0, isCharging: Boolean = false) {
    val cardBackgroundColor = if (percentage >= 80) {
        Color(0xFF1DFC4E)
    } else if (percentage >= 20) {
        Color(0xFFFFAC6A)
    } else {
        Color(0xFFFF4832)
    }
    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 40.dp),
        colors = CardDefaults.cardColors(
            cardBackgroundColor
        )
    ) {
        Row() {
            if (isCharging) {
                Icon(
                    painter = painterResource(R.drawable.battery_charging_60_fill0_wght400_grad0_opsz24),
                    contentDescription = "batteryInfo",
                    Modifier.padding(5.dp)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.battery_6_bar_fill0_wght400_grad0_opsz24),
                    contentDescription = "batteryInfo",
                    Modifier.padding(5.dp)
                )
            }

            Text(
                text = "$percentage%",
                fontFamily = sourceCodeProFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(5.dp),
                textAlign = TextAlign.Left
            )
        }
    }
}

@Composable
fun QueueDetailLayout(queueDetail: QueueDetail? = null) {
    Card(
        modifier = Modifier
            .size(width = 600.dp, height = 200.dp),
        colors = CardDefaults.cardColors(
            Color(0xFFF1FFCB)
        )
    ) {

        Column() {
            Row() {
                Card {
                    Text(
                        text = "Task Detail",
                        fontSize = 20.sp,
                        fontFamily = sourceCodeProFontFamily,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .padding(10.dp)
                    )
                }
            }
            if (queueDetail != null) {
                val modifier = Modifier
                    .padding(5.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 250.dp, height = 180.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "ID: ${queueDetail.queueId}",
                                fontSize = 16.sp,
                                fontFamily = sourceCodeProFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = modifier
                            )
                            Text(
                                text = "Status: ${queueDetail.status}",
                                fontSize = 16.sp,
                                fontFamily = sourceCodeProFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = modifier
                            )
                            Text(
                                text = "From: ${queueDetail.pickupPoint}",
                                fontSize = 16.sp,
                                fontFamily = sourceCodeProFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = modifier
                            )
                            Text(
                                text = "To: ${queueDetail.destinationPoint}",
                                fontSize = 16.sp,
                                fontFamily = sourceCodeProFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = modifier
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(width = 250.dp, height = 180.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "",
                                fontSize = 16.sp,
                                fontFamily = sourceCodeProFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = modifier
                            )
                            Text(
                                text = "Job Type: ${queueDetail.jobType}",
                                fontSize = 16.sp,
                                fontFamily = sourceCodeProFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = modifier
                            )
                            Text(
                                text = "Product Type: ${queueDetail.productType}",
                                fontSize = 16.sp,
                                fontFamily = sourceCodeProFontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = modifier
                            )
                        }
                    }
                }
            } else {
                Row {
                    Column {
                        Text(
                            text = "No Queue",
                            fontSize = 30.sp,
                            fontFamily = sourceCodeProFontFamily,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .padding(20.dp)
                                .width(600.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                }

            }
        }


    }
}

@Preview(showBackground = true)
@Composable
fun ItemPreview() {
//    LineButton("Line 0", true){
//
//    }
    val queue = QueueDetail(
        queueId = 99,
        status = "Pending",
        pickupPoint = "Line 5",
        destinationPoint = "Lab",
        productType = "Good",
        jobType = "Film Thickness"
    )
    QueueDetailLayout(queue)
}