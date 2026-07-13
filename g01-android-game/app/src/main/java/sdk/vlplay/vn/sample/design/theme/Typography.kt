package sdk.vlplay.vn.sample.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3 type scale — parity iOS [VLTypography].
 */
val VLTypography = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Normal, lineHeight = 64.sp),
    displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.Normal, lineHeight = 52.sp),
    displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Normal, lineHeight = 44.sp),

    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Normal, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Normal, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Normal, lineHeight = 32.sp),

    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),

    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),

    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
)

/**
 * iOS-named TextStyle aliases — parity demo2 [VLFont] Swift enum.
 * Use these directly when porting iOS views to keep size + weight identical;
 * fall back to [Typography] Material slots elsewhere.
 *
 * Note: iOS [VLFont.labelBold] uses `lowercaseSmallCaps()`. Compose offers
 * this via `FontFeatureSettings = "smcp"` — kept identical here.
 */
object VLFont {
    val h1 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
    val h2 = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)

    val bodyLg = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
    val bodyMd = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
    val bodySm = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)

    val labelBold = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFeatureSettings = "smcp",
    )
    val labelMd = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)

    val code = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Monospace,
    )
    val codeMd = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace,
    )

    // Legacy aliases mirroring iOS demo2 Design/VLTypography.swift
    val title = h1
    val sectionTitle = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    val rowTitle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
    val caption = bodySm
    val button = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    val hero = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
}
