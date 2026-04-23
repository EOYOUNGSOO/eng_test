package com.euysoo.engtest.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.euysoo.engtest.data.entity.PartOfSpeech
import com.euysoo.engtest.data.entity.WordDifficulty
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class ParsedWord(
    val word: String,
    val partOfSpeech: String = "",
    val meaning: String,
    val isSelected: Boolean = true,
    /** OCR 사전 자동보정으로 스펠링이 변경된 항목 여부 */
    val isAutoCorrected: Boolean = false,
    /** 단어장 저장 시 신규 Word 행에 반영 (기존 단어는 DB 값 유지) */
    val difficulty: WordDifficulty = WordDifficulty.HIGH,
)

/** ML Kit 기반 표 OCR 한 행의 정규화 결과 (신뢰도는 컬럼 매칭 품질 추정용). */
data class OcrWordEntry(
    val word: String,
    val partOfSpeech: String,
    val meaning: String,
    val confidence: Float = 1f,
) {
    fun toParsedWord(): ParsedWord {
        val w = word.trim().lowercase()
        val p = OcrHelper.normalizePartOfSpeech(partOfSpeech)
        val m = normalizeOcrMeaning(meaning)
        return ParsedWord(
            word = w,
            partOfSpeech = p,
            meaning = m,
            isSelected = OcrHelper.isOcrRowComplete(w, p, m),
        )
    }
}

sealed class OcrException(message: String) : Exception(message) {
    object EngineFailure : OcrException("OCR 엔진 오류")

    object NoParsedWords : OcrException("인식된 단어 없음")

    object LowQualityResult : OcrException("인식 품질 불량")

    object ImageTooSmall : OcrException("이미지 해상도 부족")

    object RateLimitExceeded : OcrException("API 호출 한도 초과 (분당 15회)")

    object InvalidApiKey : OcrException("Gemini API 키 오류")
}

// ──────────────────────────────────────────────────────────────────
//  품사 매핑표 (POS_MAP) — OCR 변형 → 표준 품사
// ──────────────────────────────────────────────────────────────────
private val POS_MAP: Map<String, String> =
    mapOf(
        "n" to "n.",
        "n." to "n.",
        "n ." to "n.",
        "n," to "n.",
        "noun" to "n.",
        "v" to "v.",
        "v." to "v.",
        "v ." to "v.",
        "v," to "v.",
        "verb" to "v.",
        "vt" to "v.",
        "vt." to "v.",
        "vi" to "v.",
        "vi." to "v.",
        "adj" to "adj.",
        "adj." to "adj.",
        "adj ." to "adj.",
        "adj," to "adj.",
        "adjective" to "adj.",
        "a" to "adj.",
        "a." to "adj.",
        "adv" to "adv.",
        "adv." to "adv.",
        "adv ." to "adv.",
        "adv," to "adv.",
        "adverb" to "adv.",
        "prep" to "prep.",
        "prep." to "prep.",
        "prep ." to "prep.",
        "prep," to "prep.",
        "preposition" to "prep.",
        "conj" to "conj.",
        "conj." to "conj.",
        "conj ." to "conj.",
        "conj," to "conj.",
        "conjunction" to "conj.",
        "pron" to "pron.",
        "pron." to "pron.",
        "pron ." to "pron.",
        "pron," to "pron.",
        "pronoun" to "pron.",
        "int" to "int.",
        "int." to "int.",
        "int ." to "int.",
        "interj" to "int.",
        "interj." to "int.",
        "interjection" to "int.",
        "aux" to "aux.",
        "aux." to "aux.",
        "aux ." to "aux.",
        "modal" to "aux.",
        "det" to "det.",
        "det." to "det.",
        "det ." to "det.",
        "determiner" to "det.",
        "article" to "det.",
        "num" to "num.",
        "num." to "num.",
        "num ." to "num.",
        "numeral" to "num.",
        "abbr" to "abbr.",
        "abbr." to "abbr.",
        "abbreviation" to "abbr.",
    )

private fun lookupPos(token: String): String? {
    val key =
        token
            .trim()
            .lowercase()
            .replace(Regex("""\s+"""), " ")
            .replace(Regex("""\s*([.,])\s*"""), "$1")
    POS_MAP[key]?.let { return it }
    val stripped = key.trimEnd('.', ',', ' ')
    POS_MAP[stripped]?.let { return it }
    return null
}

/** 품사 약어만 있는 줄·오인식 행 제거용 (표 헤더·품사 열 단독 등) */
private fun isPosOnlyLatinToken(text: String): Boolean {
    val t = text.trim().lowercase()
    if (t.isEmpty()) return false
    if (lookupPos(t) != null) return true
    return t.matches(Regex("""^(n|v|adj|adv|prep|conj|pron|int|det|aux|num)(\.|,)?$"""))
}

/** OCR이 품사 마침표·구분자를 뜻 앞에 붙인 경우 제거 */
internal fun normalizeOcrMeaning(raw: String): String {
    var s = raw.trim()
    s =
        s.replace(Regex("""^[\s.,·‧\u00B7\u318Dㆍ]+"""), "")
    s =
        s.replace(Regex("""^[\uAC00-\uD7A3]{1,2}\s*[.,]\s*"""), "")
    return s.trim()
}

private val TABLE_HEADER_WORDS =
    setOf(
        "word",
        "words",
        "단어",
        "품사",
        "뜻",
        "meaning",
        "part",
        "of",
        "speech",
        "pos",
        "no",
        "번호",
    )

private val SINGLE_TOKEN_HEADER_LATIN =
    setOf("word", "words", "meaning")

private val SINGLE_TOKEN_HEADER_KR =
    setOf("단어", "품사", "뜻")

private fun isLikelyTableHeaderLine(latinLine: String): Boolean {
    val t = latinLine.trim().lowercase()
    if (t.isEmpty()) return true
    val tokens =
        t
            .split(Regex("""\s+"""))
            .map { it.trim().trimEnd('.', ',', ')', '(') }
            .filter { it.isNotBlank() }
    if (tokens.isEmpty()) return true
    if (tokens.size == 1) {
        val only = tokens[0]
        val low = only.lowercase()
        if (low in SINGLE_TOKEN_HEADER_LATIN) return true
        if (only in SINGLE_TOKEN_HEADER_KR) return true
    }
    if (tokens.all { it.lowercase() in TABLE_HEADER_WORDS }) return true
    return false
}

object OcrHelper {
    /** 한 라인에서 단어 / 품사 / 뜻 추출 (ML Kit 라인 텍스트 폴백). */
    private val OCR_LINE_PATTERN =
        Regex(
            """^([a-zA-Z\-]+)\s+(adj|adv|v|n|prep|conj|pron|interj|art)\.?\s+(.+)$""",
            RegexOption.IGNORE_CASE,
        )

    private val ADJACENT_POS_PATTERN =
        Regex(
            """^(adj|adv|v|n|prep|conj|pron|interj|art)\.?$""",
            RegexOption.IGNORE_CASE,
        )

    /**
     * OCR·레거시 영어 약자 등을 **한글 품사 label**로 정규화해 DB에 저장한다.
     * 이미 한글(복수는 쉼표 구분)이면 검증 후 그대로 정리한다.
     */
    fun normalizePartOfSpeech(raw: String): String {
        val trimmed =
            raw
                .trim()
                .replace(Regex("""예\s*:""", RegexOption.IGNORE_CASE), "")
                .replace(":", "")
                .trim()
        if (trimmed.isEmpty()) return ""
        val segments = PartOfSpeech.parseLabels(trimmed)
        if (segments.isEmpty()) return ""
        val resolved =
            segments.mapNotNull { token ->
                val t =
                    token
                        .trim()
                        .trimEnd('.', ' ', '　')
                        .lowercase()
                        .replace(Regex("""\s+"""), "")
                val abbrKey =
                    when (t) {
                        "int" -> "interj"
                        "det", "article" -> "art"
                        else -> t
                    }
                PartOfSpeech.fromLabel(token.trim())?.label
                    ?: PartOfSpeech.fromAbbr(abbrKey)?.label
            }.distinct()
        return if (resolved.isEmpty()) "" else PartOfSpeech.toStorageString(resolved)
    }

    /** 단어·뜻이 있고, 품사가 한글 정의에 맞게 하나 이상 선택된 행만 OCR 저장 후보로 본다. */
    fun isOcrRowComplete(
        word: String,
        partOfSpeech: String,
        meaning: String,
    ): Boolean {
        if (word.trim().isBlank() || normalizeOcrMeaning(meaning).isBlank()) return false
        val labels = PartOfSpeech.parseLabels(partOfSpeech.trim())
        if (labels.isEmpty()) return false
        return labels.all { PartOfSpeech.fromLabel(it) != null }
    }

    /**
     * ML Kit [Text]에서 3컬럼 표(단어|품사|뜻)를 추출한다.
     * 전략 A: X좌표 클러스터링 → 행(Y) 그룹
     * 전략 B: 라인 단위 정규식
     * 전략 C: 인접 블록에서 품사 패턴 보완
     */
    fun parseOcrResult(
        latin: Text,
        korean: Text,
    ): List<ParsedWord> {
        val atoms = collectOcrAtoms(latin, korean)
        if (atoms.isEmpty()) return emptyList()

        val clustered =
            buildRowsFromColumnClustering(atoms)
                .map { it.toParsedWord() }
                .filter { it.word.isNotBlank() && !isLikelyTableHeaderLine(it.word) }

        val regexFromLatin = parseLatinLinesWithRegex(latin)
        val merged =
            if (clustered.isEmpty()) {
                regexFromLatin
            } else {
                mergeColumnAndRegexRows(clustered, regexFromLatin)
            }

        val withPosFix = applyAdjacentPosPatch(merged, atoms)
        return dedupeWords(withPosFix.map { w ->
            val p = normalizePartOfSpeech(w.partOfSpeech)
            val m = normalizeOcrMeaning(w.meaning)
            w.copy(
                partOfSpeech = p,
                meaning = m,
                isSelected = isOcrRowComplete(w.word, p, m),
            )
        })
    }

    fun mergeAndParse(
        latin: Text,
        korean: Text,
    ): List<ParsedWord> {
        val structured = parseOcrResult(latin, korean)
        val lineBased = mergeAndParseLineBased(latin, korean)

        fun score(words: List<ParsedWord>): Int {
            if (words.isEmpty()) return 0
            val pos = words.count { it.partOfSpeech.isNotBlank() }
            val mean = words.count { it.meaning.isNotBlank() }
            return words.size * 8 + pos * 6 + mean * 3
        }

        return if (score(structured) >= score(lineBased)) structured else lineBased
    }

    private enum class OcrAtomKind {
        Latin,
        Hangul,
        Mixed,
    }

    private data class OcrAtom(
        val text: String,
        val rect: Rect,
        val kind: OcrAtomKind,
    )

    private fun classifyAtomKind(raw: String): OcrAtomKind {
        val hasHangul = raw.any { it in '\uAC00'..'\uD7A3' }
        val hasAsciiLetter = raw.any { (it in 'a'..'z') || (it in 'A'..'Z') }
        return when {
            hasHangul && hasAsciiLetter -> OcrAtomKind.Mixed
            hasHangul -> OcrAtomKind.Hangul
            else -> OcrAtomKind.Latin
        }
    }

    private fun collectOcrAtoms(
        latin: Text,
        korean: Text,
    ): List<OcrAtom> {
        val atoms = mutableListOf<OcrAtom>()
        for (block in latin.textBlocks.orEmpty()) {
            for (line in block.lines.orEmpty()) {
                val els = line.elements.orEmpty()
                if (els.isNotEmpty()) {
                    for (el in els) {
                        val raw = el.text.trim()
                        if (raw.isBlank()) continue
                        val cleaned = cleanLatinLine(raw)
                        if (cleaned.isBlank()) continue
                        val box = el.boundingBox ?: line.boundingBox ?: continue
                        atoms.add(OcrAtom(cleaned, box, classifyAtomKind(cleaned)))
                    }
                } else {
                    val cleaned = cleanLatinLine(line.text)
                    if (cleaned.isBlank()) continue
                    val box = line.boundingBox ?: continue
                    atoms.add(OcrAtom(cleaned, box, classifyAtomKind(cleaned)))
                }
            }
        }
        for (block in korean.textBlocks.orEmpty()) {
            for (line in block.lines.orEmpty()) {
                val raw = line.text.trim()
                if (raw.isBlank()) continue
                val kor =
                    raw
                        .filter {
                            it in '\uAC00'..'\uD7A3' ||
                                it == ',' ||
                                it == ' ' ||
                                it == '·' ||
                                it == '.' ||
                                it == '…' ||
                                it == '(' ||
                                it == ')' ||
                                it == '/' ||
                                it == '~' ||
                                (it in '0'..'9')
                        }.trim()
                if (kor.isNotBlank()) {
                    val box = line.boundingBox ?: continue
                    atoms.add(OcrAtom(kor, box, OcrAtomKind.Hangul))
                }
            }
        }
        return atoms
    }

    private fun kMeans1dAssignCluster(
        values: List<Float>,
        k: Int = 3,
    ): IntArray {
        if (values.isEmpty()) return intArrayOf()
        val sorted = values.sorted()
        val centers =
            FloatArray(k) { idx ->
                sorted[(idx * sorted.lastIndex / max(1, k - 1)).coerceIn(0, sorted.lastIndex)]
            }
        val assignments = IntArray(values.size)
        repeat(14) {
            for (i in values.indices) {
                var best = 0
                var bestD = Float.MAX_VALUE
                for (c in 0 until k) {
                    val d = abs(values[i] - centers[c])
                    if (d < bestD) {
                        bestD = d
                        best = c
                    }
                }
                assignments[i] = best
            }
            val sums = FloatArray(k)
            val counts = IntArray(k)
            for (i in values.indices) {
                val c = assignments[i]
                sums[c] += values[i]
                counts[c]++
            }
            for (c in 0 until k) {
                if (counts[c] > 0) {
                    centers[c] = sums[c] / counts[c]
                }
            }
        }
        return assignments
    }

    private fun inferPosFromTokensInRow(texts: List<String>): String {
        for (raw in texts) {
            for (token in raw.split(Regex("""\s+""")).map { it.trim() }.filter { it.isNotBlank() }) {
                if (ADJACENT_POS_PATTERN.matches(token)) {
                    return normalizePartOfSpeech(token)
                }
                lookupPos(token)?.let { mapped ->
                    val n = normalizePartOfSpeech(mapped)
                    if (n.isNotBlank()) return n
                }
            }
        }
        return ""
    }

    private fun extractPrimaryEnglishWord(raw: String): String {
        val spaced = splitGluedWordAndPos(cleanLatinLine(raw))
        val tokens = spaced.split(Regex("""\s+""")).map { it.trim() }.filter { it.isNotBlank() }
        val eng = Regex("""[a-zA-Z][a-zA-Z\-']*""")
        for (t in tokens) {
            if (isPosOnlyLatinToken(t)) continue
            val m = eng.find(t) ?: continue
            val w = m.value.lowercase()
            if (w.length < 2) continue
            if (isLikelyTableHeaderLine(w)) continue
            if (w.matches(eng)) return w
        }
        return ""
    }

    private fun tryLinePatternOnString(line: String): OcrWordEntry? {
        val trimmed = line.trim()
        if (trimmed.isBlank()) return null
        val noNum = trimmed.replace(Regex("""^\d+[\.\):\s]+"""), "").trim()
        val m =
            OCR_LINE_PATTERN.matchEntire(noNum)
                ?: OCR_LINE_PATTERN.matchEntire(trimmed)
        if (m == null) return null
        val w = m.groupValues[1].trim().lowercase()
        val p = normalizePartOfSpeech(m.groupValues[2])
        val mean = normalizeOcrMeaning(m.groupValues[3])
        if (w.isBlank() || isLikelyTableHeaderLine(w)) return null
        return OcrWordEntry(word = w, partOfSpeech = p, meaning = mean, confidence = 0.95f)
    }

    private fun buildRowsFromColumnClustering(atoms: List<OcrAtom>): List<OcrWordEntry> {
        val xs = atoms.map { it.rect.left.toFloat() }
        val assign = kMeans1dAssignCluster(xs, k = 3)
        val active = (0 until 3).filter { c -> assign.indices.any { assign[it] == c } }.toSet()
        if (active.size < 2) return emptyList()

        fun meanX(c: Int): Float {
            val idxs = assign.indices.filter { assign[it] == c }
            if (idxs.isEmpty()) return Float.MAX_VALUE
            return idxs.map { xs[it] }.average().toFloat()
        }

        val ordered = active.sortedBy { meanX(it) }
        val colWord = ordered.first()
        val colMean = ordered.last()
        val colPos: Int? =
            if (ordered.size >= 3) {
                ordered[1]
            } else {
                null
            }

        fun colFor(i: Int) = assign[i]

        val rowTol = 40f
        val sortedIdx = atoms.indices.sortedBy { atoms[it].rect.centerY() }
        val rows = mutableListOf<MutableList<Int>>()
        for (idx in sortedIdx) {
            val cy = atoms[idx].rect.centerY().toFloat()
            var placed = false
            for (row in rows) {
                val ref = atoms[row.first()].rect.centerY().toFloat()
                if (abs(cy - ref) <= rowTol) {
                    row.add(idx)
                    placed = true
                    break
                }
            }
            if (!placed) {
                rows.add(mutableListOf(idx))
            }
        }

        val out = mutableListOf<OcrWordEntry>()
        for (row in rows) {
            val sortedRow = row.sortedBy { atoms[it].rect.left }
            val wordParts = mutableListOf<String>()
            val posParts = mutableListOf<String>()
            val meanParts = mutableListOf<String>()
            val rowTexts = mutableListOf<String>()
            for (ai in sortedRow) {
                val atom = atoms[ai]
                rowTexts.add(atom.text)
                when {
                    colFor(ai) == colWord -> wordParts.add(atom.text)
                    colFor(ai) == colMean -> meanParts.add(atom.text)
                    colPos != null && colFor(ai) == colPos -> posParts.add(atom.text)
                    else ->
                        when (atom.kind) {
                            OcrAtomKind.Hangul -> meanParts.add(atom.text)
                            else -> wordParts.add(atom.text)
                        }
                }
            }
            val wordCol = wordParts.joinToString(" ").trim()
            val posCol = posParts.joinToString(" ").trim()
            val meanCol = meanParts.joinToString(" ").trim()

            var eng = extractPrimaryEnglishWord(wordCol)
            if (eng.isBlank()) {
                val joined = rowTexts.joinToString(" ")
                eng = extractPrimaryEnglishWord(joined)
            }
            if (eng.isBlank()) continue
            if (isLikelyTableHeaderLine(eng)) continue

            var posNorm = normalizePartOfSpeech(posCol)
            if (posNorm.isBlank()) {
                posNorm = lookupPos(posCol)?.let { normalizePartOfSpeech(it) }.orEmpty()
            }
            if (posNorm.isBlank()) {
                posNorm = inferPosFromTokensInRow(rowTexts + listOf(wordCol, posCol))
            }

            var meaning = normalizeOcrMeaning(meanCol)
            if (meaning.isBlank()) {
                meaning = normalizeOcrMeaning(rowTexts.filter { classifyAtomKind(it) == OcrAtomKind.Hangul }.joinToString(" "))
            }

            val stitched = listOf(wordCol, posCol, meanCol).filter { it.isNotBlank() }.joinToString(" ")
            tryLinePatternOnString(stitched)?.let { parsed ->
                if (parsed.meaning.isNotBlank() && meaning.isBlank()) {
                    meaning = parsed.meaning
                }
                if (parsed.partOfSpeech.isNotBlank() && posNorm.isBlank()) {
                    posNorm = parsed.partOfSpeech
                }
            }

            val conf =
                when {
                    posNorm.isNotBlank() && meaning.isNotBlank() -> 1f
                    posNorm.isNotBlank() || meaning.isNotBlank() -> 0.85f
                    else -> 0.65f
                }
            out.add(OcrWordEntry(word = eng, partOfSpeech = posNorm, meaning = meaning, confidence = conf))
        }
        return out
    }

    private fun parseLatinLinesWithRegex(latin: Text): List<ParsedWord> {
        val acc = mutableListOf<ParsedWord>()
        for (block in latin.textBlocks.orEmpty()) {
            for (line in block.lines.orEmpty()) {
                val cleaned = cleanLatinLine(line.text).trim()
                if (cleaned.isBlank()) continue
                tryLinePatternOnString(cleaned)?.let { acc.add(it.toParsedWord()) }
            }
        }
        return acc
    }

    private fun mergeColumnAndRegexRows(
        column: List<ParsedWord>,
        regexRows: List<ParsedWord>,
    ): List<ParsedWord> {
        if (regexRows.isEmpty()) return column
        val byWord = regexRows.groupBy { it.word.lowercase() }
        return column.map { c ->
            val candidates = byWord[c.word.lowercase()].orEmpty()
            if (candidates.isEmpty()) return@map c
            val pick =
                candidates.maxByOrNull { r ->
                    var s = 0
                    if (r.partOfSpeech.isNotBlank()) s += 3
                    if (r.meaning.isNotBlank()) s += 2
                    s
                } ?: return@map c
            c.copy(
                partOfSpeech = c.partOfSpeech.ifBlank { pick.partOfSpeech },
                meaning = c.meaning.ifBlank { pick.meaning },
            )
        }
    }

    private fun findPosFromNearbyAtoms(
        word: ParsedWord,
        atoms: List<OcrAtom>,
    ): String {
        val wlow = word.word.lowercase()
        val hits =
            atoms.filter { a ->
                a.kind != OcrAtomKind.Hangul &&
                    wlow in a.text.lowercase().replace(Regex("""\s+"""), "")
            }
        val y = hits.minOfOrNull { it.rect.centerY() } ?: return ""
        val rowAtoms =
            atoms.filter {
                abs(it.rect.centerY() - y) <= 40f
            }.sortedBy { it.rect.left }
        for (a in rowAtoms) {
            val t = a.text.trim()
            if (ADJACENT_POS_PATTERN.matches(t)) return normalizePartOfSpeech(t)
            lookupPos(t)?.let { mapped ->
                val n = normalizePartOfSpeech(mapped)
                if (n.isNotBlank()) return n
            }
        }
        return ""
    }

    private fun applyAdjacentPosPatch(
        words: List<ParsedWord>,
        atoms: List<OcrAtom>,
    ): List<ParsedWord> =
        words.map { w ->
            if (w.partOfSpeech.isNotBlank()) return@map w
            val fromAtoms = findPosFromNearbyAtoms(w, atoms)
            if (fromAtoms.isNotBlank()) w.copy(partOfSpeech = fromAtoms) else w
        }

    data class WordCorrection(
        val corrected: String,
        val score: Int,
    )

    data class OcrRecognitionResult(
        val words: List<ParsedWord>,
        val hasAnyText: Boolean,
        val debugReport: String,
    )

    data class OcrCandidateScore(
        val label: String,
        val total: Int,
        val unique: Int,
        val meanings: Int,
        val pos: Int,
        val score: Int,
    )

    suspend fun recognizeBoth(bitmap: Bitmap): Pair<Text, Text> =
        coroutineScope {
            val image = InputImage.fromBitmap(bitmap, 0)
            val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val koreanRecognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            val latinJob = async { recognizeWithClient(image, latinRecognizer) }
            val koreanJob = async { recognizeWithClient(image, koreanRecognizer) }
            Pair(latinJob.await(), koreanJob.await())
        }

    /**
     * 인식률 향상을 위해 원본 + 전처리 변형 OCR를 시도하고, 품질 점수가 가장 높은 결과를 선택한다.
     * 변형 OCR 중 일부가 실패해도 나머지로 계속 진행하며, 최종적으로 예외를 밖으로 던지지 않는다.
     */
    suspend fun recognizeAndParseBest(bitmap: Bitmap): OcrRecognitionResult =
        try {
            recognizeAndParseBestInternal(bitmap)
        } catch (e: Exception) {
            AppLogger.e("OcrHelper", "recognizeAndParseBest failed, using single-pass fallback", e)
            singlePassRecognize(bitmap, "fatal:${e.javaClass.simpleName}")
        }

    private suspend fun recognizeAndParseBestInternal(bitmap: Bitmap): OcrRecognitionResult {
        val variants = buildBitmapVariants(bitmap)
        var hasAnyText = false
        val candidates = mutableListOf<Pair<String, List<ParsedWord>>>()
        try {
            for ((label, variant) in variants) {
                try {
                    val (latin, korean) = recognizeBoth(variant)
                    if (latin.text.isNotBlank() || korean.text.isNotBlank()) {
                        hasAnyText = true
                    }
                    val merged = mergeAndParse(latin, korean)
                    if (merged.isNotEmpty()) {
                        candidates.add("$label:merge" to merged)
                    }
                    if (latin.text.isNotBlank()) {
                        val fallback = parseWordListRaw(latin.text)
                        if (fallback.isNotEmpty()) {
                            candidates.add("$label:fallback" to fallback)
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.w("OcrHelper", "OCR variant failed: $label", e)
                }
            }
        } finally {
            variants.forEach { (_, bmp) ->
                if (bmp !== bitmap) {
                    runCatching { if (!bmp.isRecycled) bmp.recycle() }
                }
            }
        }

        if (candidates.isEmpty()) {
            val fallback = singlePassRecognize(bitmap, "no-candidates")
            return OcrRecognitionResult(
                words = fallback.words,
                hasAnyText = hasAnyText || fallback.hasAnyText,
                debugReport = "${fallback.debugReport} ; tried=${variants.size}",
            )
        }
        val scored =
            candidates.map { (label, words) ->
                val unique = words.map { it.word.trim().lowercase() }.filter { it.isNotBlank() }.toSet().size
                val meanings = words.count { it.meaning.isNotBlank() }
                val pos = words.count { it.partOfSpeech.isNotBlank() }
                val score = (unique * 6) + (meanings * 2) + pos
                OcrCandidateScore(label, words.size, unique, meanings, pos, score)
            }
        val bestScore = scored.maxByOrNull { it.score }
        val bestWords = candidates.firstOrNull { it.first == bestScore?.label }?.second.orEmpty()
        val report = buildDebugReport(scored, bestScore)
        return OcrRecognitionResult(
            words = dedupeWords(bestWords),
            hasAnyText = hasAnyText,
            debugReport = report,
        )
    }

    /** 기존 단일 경로(라틴+한국어 병합 → 라틴 줄 파싱). 예외 시 빈 결과. */
    private suspend fun singlePassRecognize(
        bitmap: Bitmap,
        reason: String,
    ): OcrRecognitionResult =
        try {
            val (latin, korean) = recognizeBoth(bitmap)
            val has = latin.text.isNotBlank() || korean.text.isNotBlank()
            var words = mergeAndParse(latin, korean)
            if (words.isEmpty() && latin.text.isNotBlank()) {
                words = parseWordListRaw(latin.text)
            }
            OcrRecognitionResult(
                words = dedupeWords(words),
                hasAnyText = has,
                debugReport = "single:$reason",
            )
        } catch (e: Exception) {
            AppLogger.e("OcrHelper", "singlePassRecognize failed", e)
            OcrRecognitionResult(emptyList(), false, "single-fail:${e.javaClass.simpleName}")
        }

    private fun buildBitmapVariants(source: Bitmap): List<Pair<String, Bitmap>> {
        val variants = mutableListOf<Pair<String, Bitmap>>()
        variants.add("orig" to source)
        variants.add("gray-contrast" to enhanceBitmap(source, grayscale = true, contrast = 1.22f, brightness = 8f))
        val longEdge = maxOf(source.width, source.height)
        if (longEdge <= 2400) {
            variants.add(
                "upscale-gray-contrast" to
                    enhanceBitmap(upscaleBitmap(source, 1.35f), grayscale = true, contrast = 1.32f, brightness = 8f),
            )
        }
        return variants.distinctBy { System.identityHashCode(it.second) }
    }

    private fun buildDebugReport(
        scored: List<OcrCandidateScore>,
        best: OcrCandidateScore?,
    ): String {
        val lines =
            scored.joinToString(" | ") { s ->
                "${s.label} total=${s.total} unique=${s.unique} mean=${s.meanings} pos=${s.pos} score=${s.score}"
            }
        val winner = best?.label ?: "none"
        return "winner=$winner ; $lines"
    }

    private fun upscaleBitmap(source: Bitmap, scale: Float): Bitmap {
        if (scale <= 1f) return source
        val targetW = (source.width * scale).toInt().coerceAtLeast(source.width)
        val targetH = (source.height * scale).toInt().coerceAtLeast(source.height)
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    private fun enhanceBitmap(
        source: Bitmap,
        grayscale: Boolean,
        contrast: Float,
        brightness: Float,
    ): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val matrix =
            if (grayscale) {
                ColorMatrix().apply { setSaturation(0f) }
            } else {
                ColorMatrix()
            }
        val contrastMatrix =
            ColorMatrix(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )
        matrix.postConcat(contrastMatrix)
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    private suspend fun recognizeWithClient(
        image: InputImage,
        recognizer: TextRecognizer,
    ): Text =
        suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation { runCatching { recognizer.close() } }
            recognizer
                .process(image)
                .addOnSuccessListener {
                    runCatching { recognizer.close() }
                    cont.resume(it)
                }.addOnFailureListener { e ->
                    runCatching { recognizer.close() }
                    cont.resumeWithException(e)
                }
        }

    private fun mergeAndParseLineBased(
        latin: Text,
        korean: Text,
    ): List<ParsedWord> {
        data class LatinLine(val text: String, val rect: Rect)

        val rawLatinLines = mutableListOf<LatinLine>()
        for (block in latin.textBlocks.orEmpty()) {
            for (line in block.lines.orEmpty()) {
                val cleaned = cleanLatinLine(line.text)
                if (cleaned.isNotBlank()) {
                    rawLatinLines.add(LatinLine(cleaned, line.boundingBox ?: Rect()))
                }
            }
        }

        val latinLines = mutableListOf<LatinLine>()
        if (rawLatinLines.isNotEmpty()) {
            rawLatinLines.sortWith(compareBy<LatinLine> { it.rect.centerY() }.thenBy { it.rect.left })
            val avgLineHeight =
                rawLatinLines
                    .map { it.rect.height() }
                    .average()
                    .coerceAtLeast(10.0)
            val mergeThreshold = avgLineHeight * 0.85
            var i = 0
            while (i < rawLatinLines.size) {
                val current = rawLatinLines[i]
                val next = rawLatinLines.getOrNull(i + 1)
                val isNearRow =
                    next != null &&
                        abs(current.rect.exactCenterY() - next.rect.exactCenterY()) < mergeThreshold
                val isLeftToRight = next != null && next.rect.left >= current.rect.left
                val nextText = next?.text?.trim().orEmpty()
                val nextLooksLikePos = nextText.isNotBlank() && lookupPos(nextText) != null
                val currentLen = current.text.length
                val isWordFragment =
                    current.text.matches(Regex("""[a-zA-Z]{1,5}""")) &&
                        currentLen <= 5 &&
                        lookupPos(current.text.trim()) == null
                if (
                    isWordFragment &&
                        isNearRow &&
                        isLeftToRight &&
                        next != null &&
                        !nextLooksLikePos
                ) {
                    val mergedRect =
                        Rect(
                            minOf(current.rect.left, next.rect.left),
                            minOf(current.rect.top, next.rect.top),
                            maxOf(current.rect.right, next.rect.right),
                            maxOf(current.rect.bottom, next.rect.bottom),
                        )
                    val mergedText = current.text.trimEnd() + next.text.trimStart()
                    latinLines.add(LatinLine(cleanLatinLine(mergedText), mergedRect))
                    i += 2
                } else {
                    latinLines.add(current)
                    i += 1
                }
            }
        }

        data class KoreanBlock(val text: String, val rect: Rect)

        val koreanBlocks = mutableListOf<KoreanBlock>()
        for (block in korean.textBlocks.orEmpty()) {
            for (line in block.lines.orEmpty()) {
                val kor =
                    line.text
                        .filter {
                            it in '\uAC00'..'\uD7A3' ||
                                it == ',' ||
                                it == ' ' ||
                                it == '·' ||
                                it == '.' ||
                                it == '…' ||
                                it == '(' ||
                                it == ')' ||
                                it == '/' ||
                                it == '~' ||
                                (it in '0'..'9')
                        }
                        .trim()
                if (kor.isNotBlank()) {
                    koreanBlocks.add(KoreanBlock(kor, line.boundingBox ?: Rect()))
                }
            }
        }
        koreanBlocks.sortWith(compareBy<KoreanBlock> { it.rect.centerY() }.thenBy { it.rect.left })

        val rowThreshold =
            if (latinLines.isNotEmpty()) {
                latinLines.map { it.rect.height().toDouble() }.average() * 0.6
            } else {
                30.0
            }

        val latinRightEdge = latinLines.maxOfOrNull { it.rect.right } ?: 0
        val koreanColumnLeft =
            if (latinRightEdge > 0) {
                (latinRightEdge - 80).coerceAtLeast(0)
            } else {
                0
            }

        val result = mutableListOf<ParsedWord>()
        for (ll in latinLines) {
            if (isLikelyTableHeaderLine(ll.text)) continue
            val lCenterY = ll.rect.exactCenterY()
            val sameRow =
                koreanBlocks.filter { abs(it.rect.exactCenterY() - lCenterY) < rowThreshold }
            val fromMeaningColumn =
                if (koreanColumnLeft > 0) {
                    sameRow.filter { it.rect.left >= koreanColumnLeft }
                } else {
                    emptyList()
                }
            val chosen =
                if (fromMeaningColumn.isNotEmpty()) {
                    fromMeaningColumn
                } else {
                    sameRow
                }
            val matchedKor =
                chosen
                    .sortedBy { it.rect.left }
                    .joinToString(" ") { it.text }
                    .replace(Regex("""\s+"""), " ")
                    .trim()

            val parsed = parseSingleLine(ll.text, matchedKor)
            if (parsed != null) {
                result.add(parsed)
            }
        }
        return result
    }

    private fun parseSingleLine(
        latinLine: String,
        korMeaning: String,
    ): ParsedWord? {
        val noNumber =
            latinLine
                .trimStart()
                .replace(Regex("""^\d+[\.\):\s]+"""), "")
                .trim()
        if (noNumber.isBlank()) return null
        if (isLikelyTableHeaderLine(noNumber)) return null

        val latinSpaced = splitGluedWordAndPos(noNumber)

        val tokens =
            latinSpaced
                .split(Regex("""\s+|\t"""))
                .map { it.trim() }
                .filter { it.isNotBlank() }
        if (tokens.isEmpty()) return null

        var posIndex = -1
        var posValue = ""
        for (i in tokens.indices) {
            val direct = lookupPos(tokens[i])
            if (direct != null) {
                posIndex = i
                posValue = direct
                break
            }
            if (i + 1 < tokens.size) {
                val merged = lookupPos("${tokens[i]} ${tokens[i + 1]}")
                if (merged != null) {
                    posIndex = i
                    posValue = merged
                    break
                }
            }
        }

        val wordTokens =
            if (posIndex >= 0) {
                tokens.take(posIndex)
            } else {
                tokens
            }
        val wordCandidate =
            normalizeWordToken(
                wordTokens
                    .joinToString("")
                    .replace(Regex("""[^a-zA-Z0-9\-']"""), ""),
            )
        if (!engWordLooksValid(wordCandidate, posIndex >= 0)) return null

        val meaningNorm = normalizeOcrMeaning(korMeaning)
        if (isPosOnlyLatinToken(wordCandidate) && meaningNorm.isBlank()) return null

        val normPos = normalizePartOfSpeech(posValue)
        return ParsedWord(
            word = wordCandidate.lowercase(),
            partOfSpeech = normPos,
            meaning = meaningNorm,
            isSelected = isOcrRowComplete(wordCandidate.lowercase(), normPos, meaningNorm),
        )
    }

    /**
     * 품사만 있는 토큰을 영단어로 오인한 경우·지나치게 짧은 잡음 제거.
     * [hasPosInLine] 이면 같은 줄에서 품사를 이미 찾은 합법적 단어.
     */
    private fun engWordLooksValid(
        word: String,
        hasPosInLine: Boolean,
    ): Boolean {
        val engPattern = Regex("""[a-zA-Z][a-zA-Z\-']*""")
        if (!engPattern.matches(word)) return false
        val w = word.lowercase()
        if (!hasPosInLine && isPosOnlyLatinToken(w)) return false
        if (!hasPosInLine && w.length <= 2 && lookupPos(w) != null) return false
        return true
    }

    private fun normalizeWordToken(raw: String): String =
        raw
            .replace('0', 'o')
            .replace('1', 'l')
            .replace('5', 's')
            .replace(Regex("""^-+|-+$"""), "")
            .replace(Regex("""^'+|'+$"""), "")
            .lowercase()

    private fun mergeLetterSpacedTokens(raw: String): String {
        val tokens = raw.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return ""
        val out = mutableListOf<String>()
        val acc = StringBuilder()
        fun flush() {
            if (acc.isNotEmpty()) {
                out.add(acc.toString())
                acc.clear()
            }
        }
        for (token in tokens) {
            val singleLetter = token.length == 1 && token[0].isLetter()
            if (singleLetter) {
                acc.append(token)
            } else {
                flush()
                out.add(token)
            }
        }
        flush()
        return out.joinToString("  ")
    }

    private fun splitGluedWordAndPos(line: String): String {
        var s = line
        s =
            s.replace(
                Regex(
                    """([a-zA-Z]{2,})(adj\.|adv\.|prep\.|conj\.|pron\.|int\.|det\.|aux\.|num\.|vt\.|vi\.|n\.|v\.|art\.)""",
                    RegexOption.IGNORE_CASE,
                ),
                "$1 $2",
            )
        return s
    }

    private fun cleanLatinLine(raw: String): String {
        val merged = mergeLetterSpacedTokens(raw)
        return merged
            .replace(Regex("""\b(n|v|adj|adv|prep|conj|pron|int|det|aux|num)\s+\.""", RegexOption.IGNORE_CASE), "$1.")
            .replace(Regex("""\b(n|v|adj|adv|prep|conj|pron|int|det|aux|num)\.""", RegexOption.IGNORE_CASE), "$1.")
            .trim()
    }

    private fun parseWordListRaw(rawText: String): List<ParsedWord> {
        val result = mutableListOf<ParsedWord>()
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        for (line in lines) {
            val cleaned = cleanLatinLine(line)
            val parsed = parseSingleLine(cleaned, "")
            if (parsed != null) {
                result.add(parsed)
            }
        }
        return result
    }

    fun parseWordList(rawText: String): List<ParsedWord> = dedupeWords(parseWordListRaw(rawText))

    /**
     * 같은 뜻(한글)이 중복된 행·품사 열 오인식 행을 정리한다. ML Kit 표 OCR 후처리용.
     */
    fun refineVocabularyTableResults(words: List<ParsedWord>): List<ParsedWord> {
        if (words.isEmpty()) return words
        val normalized =
            words.map { w ->
                w.copy(
                    word = w.word.trim().lowercase(),
                    partOfSpeech = normalizePosField(w.partOfSpeech),
                    meaning = normalizeOcrMeaning(w.meaning),
                )
            }
        val withoutGarbage =
            normalized.filter { w ->
                if (w.word.isBlank()) return@filter false
                if (isLikelyTableHeaderLine(w.word)) return@filter false
                if (isPosOnlyLatinToken(w.word) && w.partOfSpeech.isBlank()) return@filter false
                true
            }
        val byMeaning = linkedMapOf<String, ParsedWord>()
        for (w in withoutGarbage) {
            val key =
                w.meaning
                    .replace(Regex("""\s+"""), "")
                    .lowercase()
            if (key.isBlank()) {
                byMeaning["__empty_${w.word}_${byMeaning.size}"] = w
                continue
            }
            val prev = byMeaning[key]
            if (prev == null) {
                byMeaning[key] = w
                continue
            }
            byMeaning[key] = pickBetterDuplicateRow(prev, w)
        }
        return dedupeWords(byMeaning.values.toList()).map { w ->
            w.copy(isSelected = isOcrRowComplete(w.word, w.partOfSpeech, w.meaning))
        }
    }

    private fun pickBetterDuplicateRow(
        a: ParsedWord,
        b: ParsedWord,
    ): ParsedWord {
        fun score(p: ParsedWord): Int {
            var s = p.word.length * 3
            if (p.partOfSpeech.isNotBlank()) s += 10
            if (p.meaning.isNotBlank()) s += 2
            return s
        }
        val picked = if (score(b) > score(a)) b else a
        return picked.copy(isSelected = isOcrRowComplete(picked.word, picked.partOfSpeech, picked.meaning))
    }

    /** Gemini·ML Kit 공통: 표 헤더 제거 후 [normalizePartOfSpeech]로 한글 품사로 통일 */
    fun normalizeOcrPosAbbrev(pos: String): String {
        val t = pos.trim()
        if (t.isEmpty()) return ""
        if (t == "품사") return ""
        return normalizePartOfSpeech(t)
    }

    private fun normalizePosField(pos: String): String = normalizeOcrPosAbbrev(pos)

    private fun dedupeWords(words: List<ParsedWord>): List<ParsedWord> {
        if (words.isEmpty()) return words
        val grouped = linkedMapOf<String, ParsedWord>()
        for (item in words) {
            val key = item.word.trim().lowercase()
            if (key.isBlank()) continue
            val prev = grouped[key]
            if (prev == null) {
                grouped[key] =
                    item.copy(
                        word = key,
                        isSelected = isOcrRowComplete(key, item.partOfSpeech, item.meaning),
                    )
                continue
            }
            val merged =
                prev.copy(
                    partOfSpeech = if (prev.partOfSpeech.isNotBlank()) prev.partOfSpeech else item.partOfSpeech,
                    meaning = if (prev.meaning.isNotBlank()) prev.meaning else item.meaning,
                    isAutoCorrected = prev.isAutoCorrected || item.isAutoCorrected,
                )
            grouped[key] =
                merged.copy(
                    isSelected =
                        isOcrRowComplete(merged.word, merged.partOfSpeech, merged.meaning) &&
                            (prev.isSelected || item.isSelected),
                )
        }
        return grouped.values.toList()
    }

    fun correctWordsWithDictionary(
        words: List<ParsedWord>,
        dictionary: Collection<String>,
    ): List<ParsedWord> {
        if (words.isEmpty() || dictionary.isEmpty()) return words
        val normalizedDict =
            dictionary
                .asSequence()
                .map { it.trim().lowercase() }
                .filter { it.length >= 3 }
                .toSet()
        if (normalizedDict.isEmpty()) return words

        return dedupeWords(words).map { parsed ->
            val raw = parsed.word.trim().lowercase()
            if (raw.length < 3 || raw in normalizedDict) return@map parsed
            val correction = findBestCorrection(raw, normalizedDict) ?: return@map parsed
            parsed.copy(word = correction.corrected, isAutoCorrected = true)
        }
    }

    private fun findBestCorrection(
        raw: String,
        dictionary: Set<String>,
    ): WordCorrection? {
        val first = raw.firstOrNull() ?: return null
        val last = raw.lastOrNull() ?: return null

        val candidates =
            dictionary.asSequence().filter { candidate ->
                val lenDiff = candidate.length - raw.length
                candidate.length in raw.length..(raw.length + 4) &&
                    lenDiff >= 0 &&
                    candidate.firstOrNull() == first &&
                    (candidate.lastOrNull() == last || lenDiff >= 2)
            }

        var best: WordCorrection? = null
        var secondBest: WordCorrection? = null
        for (candidate in candidates) {
            val score = candidateScore(raw, candidate)
            if (score == Int.MAX_VALUE) continue
            val current = WordCorrection(candidate, score)
            if (best == null || score < best.score) {
                secondBest = best
                best = current
            } else if (secondBest == null || score < secondBest.score) {
                secondBest = current
            }
        }

        val top = best ?: return null
        val confident = top.score <= 3
        val separatedFromSecond = secondBest == null || secondBest.score - top.score >= 1
        return if (confident && separatedFromSecond) top else null
    }

    private fun candidateScore(
        raw: String,
        candidate: String,
    ): Int {
        if (raw == candidate) return 0
        val subseqPenalty = subsequencePenalty(raw, candidate)
        if (subseqPenalty != null) {
            // 누락 글자 복원 중심 점수: skipped가 적고 접두/접미가 맞으면 우선.
            val lenGap = abs(candidate.length - raw.length)
            val prefixBonus = if (candidate.startsWith(raw)) -2 else 0
            val suffixBonus = if (candidate.endsWith(raw.takeLast(1))) -1 else 0
            return subseqPenalty + lenGap + prefixBonus + suffixBonus
        }

        val distance = boundedEditDistance(raw, candidate, 2)
        return if (distance <= 2) {
            4 + distance
        } else {
            Int.MAX_VALUE
        }
    }

    private fun subsequencePenalty(
        raw: String,
        candidate: String,
    ): Int? {
        var i = 0
        var skipped = 0
        for (c in candidate) {
            if (i < raw.length && raw[i] == c) {
                i++
            } else {
                skipped++
            }
        }
        return if (i == raw.length) skipped else null
    }

    private fun boundedEditDistance(
        a: String,
        b: String,
        maxDistance: Int,
    ): Int {
        if (abs(a.length - b.length) > maxDistance) return maxDistance + 1
        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)

        for (i in 1..a.length) {
            curr[0] = i
            var rowMin = curr[0]
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] =
                    min(
                        min(prev[j] + 1, curr[j - 1] + 1),
                        prev[j - 1] + cost,
                    )
                if (curr[j] < rowMin) rowMin = curr[j]
            }
            if (rowMin > maxDistance) return maxDistance + 1
            for (j in prev.indices) prev[j] = curr[j]
        }
        return prev[b.length]
    }

    /**
     * 갤러리·Compose 등에서 전달된 Bitmap이 비동기 처리 도중 recycle 되는 문제를 피하기 위해 복사한다.
     */
    fun safeCopyForOcr(source: Bitmap): Bitmap? =
        runCatching {
            if (source.isRecycled) return@runCatching null
            if (source.width <= 0 || source.height <= 0) return@runCatching null
            source.copy(Bitmap.Config.ARGB_8888, false)
        }.getOrNull()

    fun validateImageSize(bitmap: Bitmap): OcrException? =
        if (bitmap.width < 400 || bitmap.height < 400) {
            OcrException.ImageTooSmall
        } else {
            null
        }

    fun validateParseResult(words: List<ParsedWord>): OcrException? {
        if (words.isEmpty()) return OcrException.NoParsedWords
        val noMeaningRatio = words.count { it.meaning.isBlank() }.toFloat() / words.size
        if (noMeaningRatio > 0.7f) return OcrException.LowQualityResult
        return null
    }
}
