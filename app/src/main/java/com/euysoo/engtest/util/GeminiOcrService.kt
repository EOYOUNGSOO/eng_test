package com.euysoo.engtest.util

import android.graphics.Bitmap
import com.euysoo.engtest.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Gemini Flash API 기반 단어표 OCR. [ParsedWord] 목록으로 정규화해 반환한다.
 *
 * API 키: `local.properties` → `gemini.api.key` → [BuildConfig.GEMINI_API_KEY]
 */
object GeminiOcrService {
    private const val MODEL_NAME = "gemini-1.5-flash"

    private val callTimestamps = ArrayDeque<Long>(16)
    private const val RATE_LIMIT_PER_MINUTE = 15
    private const val RATE_WINDOW_MS = 60_000L

    suspend fun extractWords(bitmap: Bitmap): List<ParsedWord> =
        withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY.trim()
            if (apiKey.isEmpty()) {
                throw OcrException.InvalidApiKey
            }

            checkRateLimit()
            recordCall()

            val prompt = buildPrompt()

            try {
                generateWithModel(
                    apiKey = apiKey,
                    useJsonMimeType = true,
                    bitmap = bitmap,
                    prompt = prompt,
                )
            } catch (e: OcrException) {
                throw e
            } catch (e: Exception) {
                val msg0 = collectMessages(e)
                val noRetry =
                    msg0.contains("429") ||
                        msg0.contains("quota", ignoreCase = true) ||
                        msg0.contains("resource exhausted", ignoreCase = true) ||
                        msg0.contains("403") ||
                        msg0.contains("401") ||
                        msg0.contains("API_KEY", ignoreCase = true) ||
                        msg0.contains("API key", ignoreCase = true)
                if (noRetry) {
                    throw mapApiFailure(e)
                }
                runCatching {
                    generateWithModel(
                        apiKey = apiKey,
                        useJsonMimeType = false,
                        bitmap = bitmap,
                        prompt = prompt,
                    )
                }.getOrElse { e2 -> throw mapApiFailure(e2) }
            }
        }

    private suspend fun generateWithModel(
        apiKey: String,
        useJsonMimeType: Boolean,
        bitmap: Bitmap,
        prompt: String,
    ): List<ParsedWord> {
        val model =
            GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = apiKey,
                generationConfig =
                    generationConfig {
                        temperature = 0f
                        topK = 1
                        maxOutputTokens = 4096
                        if (useJsonMimeType) {
                            responseMimeType = "application/json"
                        }
                    },
            )
        val response =
            model.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                },
            )
        val rawText = response.text?.trim().orEmpty()
        if (rawText.isEmpty()) return emptyList()
        return parseGeminiResponse(rawText)
    }

    private fun mapApiFailure(e: Throwable): Exception {
        val msg = collectMessages(e)
        return when {
            msg.contains("429") ||
                msg.contains("quota", ignoreCase = true) ||
                msg.contains("resource exhausted", ignoreCase = true) ->
                OcrException.RateLimitExceeded
            msg.contains("403") ||
                msg.contains("401") ||
                msg.contains("API_KEY", ignoreCase = true) ||
                msg.contains("API key", ignoreCase = true) ->
                OcrException.InvalidApiKey
            else -> OcrException.EngineFailure
        }
    }

    private fun collectMessages(e: Throwable): String {
        val parts = mutableListOf<String>()
        var cur: Throwable? = e
        var depth = 0
        while (cur != null && depth < 6) {
            cur.message?.let { parts.add(it) }
            cur = cur.cause
            depth++
        }
        return parts.joinToString(" ")
    }

    private fun buildPrompt(): String =
        """
You are an OCR assistant specialized in English vocabulary lists.
The image contains a table or list with English words and Korean meanings.

Extract ALL rows and return ONLY a JSON array. No explanation, no markdown fences.

Rules:
- "word": English word only (lowercase). Remove row numbers like "1.", "2.".
- "pos": Part of speech abbreviation ONLY: "n.", "v.", "adj.", "adv.", "prep.", etc. (Latin letters + optional period). NEVER use the Korean column title "품사" or any Korean in this field. Empty string "" if not visible.
- "meaning": Korean meaning only. Do NOT copy a leading period/dot from the POS column (e.g. meaning must NOT start with "."). Strip leading "." or spaces. Empty string "" if not found.
- Skip the table header row if it contains labels like 단어/품사/뜻 or Word/POS/Meaning — do not output those as vocabulary.
- If a row has only an English word with no meaning, still include it with meaning "".
- Do NOT output POS abbreviations (adj., n., v., …) as the English "word" — they belong only in "pos".
- Do NOT skip any data row.
- Output MUST be valid JSON array starting with [ and ending with ].

Example output:
[
  {"word":"ambiguous","pos":"adj.","meaning":"모호한, 중의적인"},
  {"word":"ascertain","pos":"v.","meaning":"확인하다, 알아내다"},
  {"word":"catalyst","pos":"n.","meaning":"촉매, 촉진 요인"}
]
        """.trimIndent()

    private fun isPosAbbrevOcrNoise(
        word: String,
        @Suppress("UNUSED_PARAMETER") pos: String,
    ): Boolean =
        word in POS_LIKE_STANDALONE_WORDS

    private val POS_LIKE_STANDALONE_WORDS =
        setOf(
            "adj",
            "adv",
            "n",
            "v",
            "prep",
            "conj",
            "pron",
            "int",
            "det",
            "aux",
            "num",
            "vt",
            "vi",
            "abbr",
            "art",
            "interj",
        )

    private fun parseGeminiResponse(raw: String): List<ParsedWord> {
        val startIdx = raw.indexOf('[')
        val endIdx = raw.lastIndexOf(']')
        if (startIdx == -1 || endIdx == -1 || startIdx >= endIdx) {
            return emptyList()
        }
        val jsonStr = raw.substring(startIdx, endIdx + 1)
        val wordPattern = Regex("""[a-z][a-z\-']*""")

        return runCatching {
            val arr = JSONArray(jsonStr)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                val word = obj.optString("word", "").trim().lowercase()
                if (word.isBlank() || !word.matches(wordPattern)) {
                    return@mapNotNull null
                }
                val rawPos = obj.optString("pos", "").trim()
                val meaning =
                    normalizeOcrMeaning(obj.optString("meaning", "").trim())
                val pos = OcrHelper.normalizeOcrPosAbbrev(rawPos)
                if (isPosAbbrevOcrNoise(word, pos)) {
                    return@mapNotNull null
                }
                ParsedWord(
                    word = word,
                    partOfSpeech = pos,
                    meaning = meaning,
                    isSelected = OcrHelper.isOcrRowComplete(word, pos, meaning),
                )
            }
        }.getOrDefault(emptyList())
    }

    @Synchronized
    private fun checkRateLimit() {
        val now = System.currentTimeMillis()
        while (callTimestamps.isNotEmpty() && now - callTimestamps.first() > RATE_WINDOW_MS) {
            callTimestamps.removeFirst()
        }
        if (callTimestamps.size >= RATE_LIMIT_PER_MINUTE) {
            throw OcrException.RateLimitExceeded
        }
    }

    @Synchronized
    private fun recordCall() {
        callTimestamps.addLast(System.currentTimeMillis())
    }
}
