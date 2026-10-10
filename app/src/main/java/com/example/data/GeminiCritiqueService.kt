package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.model.FrameAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class AiCritiqueResult(
    val score: Int,
    val compositionCritique: String,
    val lightingCritique: String,
    val improvementTips: List<String>,
    val recommendedFilter: String,
    val recommendedGrid: String,
    val isCloudAiGenerated: Boolean
)

class GeminiCritiqueService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzePhoto(
        bitmap: Bitmap,
        localAnalysis: FrameAnalysisResult
    ): AiCritiqueResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalHeuristicCritique(localAnalysis)
        }

        try {
            // Downscale bitmap for efficient transmission (max 800px on long edge)
            val maxEdge = 800
            val scale = (maxEdge.toFloat() / maxOf(bitmap.width, bitmap.height)).coerceAtMost(1f)
            val scaledWidth = (bitmap.width * scale).toInt()
            val scaledHeight = (bitmap.height * scale).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }

            val prompt = """
                You are a world-class professional photographer and photography judge.
                Analyze this photo's composition, lighting, subject placement, and color harmony.
                Return ONLY a valid JSON object with the following schema:
                {
                  "score": integer between 60 and 99,
                  "compositionCritique": "short 1-2 sentence critique of framing & rule of thirds/balance",
                  "lightingCritique": "short 1-2 sentence assessment of lighting direction and shadows",
                  "improvementTips": ["practical tip 1", "practical tip 2"],
                  "recommendedFilter": "Natural / Cinematic / Golden Hour / Monochrome / Vivid Scenery",
                  "recommendedGrid": "Rule of Thirds / Golden Spiral / Symmetry / Leading Lines / Architecture"
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray()
                    partsArray.put(JSONObject().apply { put("text", prompt) })
                    partsArray.put(JSONObject().apply {
                        val inlineData = JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", base64Image)
                        }
                        put("inlineData", inlineData)
                    })
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.4)
                }
                put("generationConfig", generationConfig)
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBodyStr = response.body?.string().orEmpty()
                val rootJson = JSONObject(responseBodyStr)
                val candidates = rootJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                if (!text.isNullOrBlank()) {
                    val critiqueJson = JSONObject(text)
                    val score = critiqueJson.optInt("score", localAnalysis.compositionScore)
                    val comp = critiqueJson.optString("compositionCritique", "Well structured frame with good subject placement.")
                    val light = critiqueJson.optString("lightingCritique", "Natural lighting with balanced contrast.")
                    val tipsArray = critiqueJson.optJSONArray("improvementTips")
                    val tips = mutableListOf<String>()
                    if (tipsArray != null) {
                        for (i in 0 until tipsArray.length()) {
                            tips.add(tipsArray.getString(i))
                        }
                    }
                    if (tips.isEmpty()) {
                        tips.add("Align focal subjects along the top horizontal third.")
                    }

                    val filter = critiqueJson.optString("recommendedFilter", "Cinematic")
                    val grid = critiqueJson.optString("recommendedGrid", "Rule of Thirds")

                    return@withContext AiCritiqueResult(
                        score = score,
                        compositionCritique = comp,
                        lightingCritique = light,
                        improvementTips = tips,
                        recommendedFilter = filter,
                        recommendedGrid = grid,
                        isCloudAiGenerated = true
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext generateLocalHeuristicCritique(localAnalysis)
    }

    private fun generateLocalHeuristicCritique(analysis: FrameAnalysisResult): AiCritiqueResult {
        val tips = mutableListOf<String>()
        val compCritique: String
        val lightCritique: String

        if (analysis.detectedFaces.isNotEmpty()) {
            val face = analysis.detectedFaces.first()
            if (kotlin.math.abs(face.headEulerAngleZ) > 8f) {
                tips.add("Level the head tilt to establish a stronger, more grounded connection with the viewer.")
            } else {
                tips.add("Subtle 3/4 head rotation creates appealing jawline separation and natural depth.")
            }
            if (face.boundingBox.top < 0.12f) {
                tips.add("Provide a little more headroom above the subject's crown for comfortable framing.")
            } else {
                tips.add("Good headroom proportion. Eye-line alignment sits close to the power points.")
            }
            compCritique = "Subject framing demonstrates strong presence with clear facial engagement."
        } else {
            if (!analysis.isHorizonLevel) {
                tips.add("Straighten the device: horizon currently tilts by ${String.format("%.1f", analysis.horizonRollDegrees)}°.")
            }
            tips.add("Place leading diagonals or secondary foreground elements in the bottom third for depth.")
            compCritique = "Scene offers expansive field of view with pleasant environmental balance."
        }

        lightCritique = when {
            analysis.averageLuminance < 70f -> "Low ambient illumination: consider brightening shadows or stabilizing shot."
            analysis.averageLuminance > 195f -> "High key lighting: watch out for highlight clipping in brighter zones."
            analysis.contrastScore > 75f -> "Dynamic high-contrast lighting creating punchy dimensionality."
            else -> "Gentle, diffused lighting providing smooth tonal gradations across the frame."
        }

        return AiCritiqueResult(
            score = analysis.compositionScore,
            compositionCritique = compCritique,
            lightingCritique = lightCritique,
            improvementTips = tips,
            recommendedFilter = analysis.recommendedFilters.firstOrNull()?.filter?.title ?: "Natural Pro",
            recommendedGrid = analysis.recommendedGrid.title,
            isCloudAiGenerated = false
        )
    }
}
