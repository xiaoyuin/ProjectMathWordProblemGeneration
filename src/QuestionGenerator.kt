import edu.stanford.nlp.simple.Sentence
import edu.stanford.nlp.simple.Token
import java.util.*

class QuestionGenerator {

    companion object {

        val clauseRule = """(DT)?(JJ.?)*(NN.?|PRP|PRP$|POS|IN|DT|CC|VBG|VBN)+(RB.?|VB.?|MD|RP)+"""

        val numericRule = """(DT)?(CD)+(RB)?(JJS|JJR|JJ)?(NNPS|NNS|NNP|NN|VBG)+"""

        val subjectPosTags = listOf("JJ", "JJR", "JJS", "NN", "NNP", "NNS", "PRP", "PRP$", "POS", "IN", "DT", "CC", "VBG", "VBN")

        val verbPosTags = listOf("RB", "RBR", "RBS", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "MD", "RP")

        val numberPosTags = listOf("CD", "DT")

        val auxiliaryMap = mapOf("VBD" to "did", "VBP" to "do", "VB" to "do", "VBZ" to "does")

        @Throws(IllegalStateException::class)
        fun generate(text: String): Pair<String, String> {

            val document = Sentence(text)

            val clauses = document.findChunk(clauseRule)

            val numerics = document.findChunk(numericRule)

            // For now only the first chunk returned will be processed
            if (clauses.isEmpty() || numerics.isEmpty()) {
                throw IllegalStateException("Can't generate question: clause or numeric not detected")
            }

            val clause = clauses.first()
            val numeric = numerics.first()

            if (clause.isEmpty() || numeric.isEmpty()) {
                throw IllegalStateException("Can't generate question: clause or numeric not detected")
            }

            // Detect subjects and verb phrases
            val subjects = mutableListOf<Token>()
            var i = -1
            for (index in 0 until clause.size) {
                val token = clause[index]
                if (token.posTag() in subjectPosTags) {
                    subjects.add(token)
                } else {
                    i = index
                    break
                }
            }
            if (i == -1) {
                throw IllegalStateException("Can't generate question: subject or verb phrases not detected")
            }
            val verbs = clause.subList(i, clause.size)

            // Detect number phrases and objects
            val numbers = mutableListOf<Token>()
            var j = -1
            for (index in 0 until numeric.size) {
                val token = numeric[index]
                if (token.posTag() in numberPosTags) {
                    numbers.add(token)
                } else {
                    j = index
                    break
                }
            }
            if (j == -1) {
                throw IllegalStateException("Can't generate question: number phrases or objects not detected")
            }

            // Remaining parts of number phrase
            val objects = numeric.subList(j, numeric.size)

            if (subjects.isEmpty() || verbs.isEmpty() || numbers.isEmpty() || objects.isEmpty()) {
                throw IllegalStateException("Can't generate question: subject or verb phrases or number phrases or objects not detected")
            }

            // Auxiliary word and verbs after stemming
            var auxiliaryWord: String = ""
            var verbsAfter: List<String>
            if (verbs.size > 1) {
                auxiliaryWord = verbs.first().originalText()
                verbsAfter = verbs.subList(1, verbs.size).map { it.originalText() }
            } else {
                auxiliaryWord = auxiliaryMap.getValue(verbs.first().posTag())
                verbsAfter = verbs.map { it.lemma() }
            }

            val remaining = text.replace(".", "")
                    .replace(clause.toSentenceString(), "")
                    .replace(numeric.toSentenceString(), "")
                    .trim()

            val question = listOf("How many",
                    objects.toSentenceString(),
                    auxiliaryWord,
                    subjects.toSentenceString(),
                    verbsAfter.join(),
                    remaining
            ).filter { it.isNotEmpty() }.join() + "?"

            return question to numbers.toSentenceString();
        }

    }

}

typealias Chunk = List<Token>

fun Chunk.toSentenceString(): String {
    return this.map { it.originalText() }.join()
}

fun Sentence.findChunk(rule: String): List<Chunk> {

    val posTags = this.posTags()
    val posIndexes = mutableListOf<Int>()

    var i = 0
    for (posTag in posTags) {
        posIndexes.add(i)
        i += posTag.length
    }

    val regex = Regex(rule)
    val matches = regex.findAll(posTags.joinToString(""))

    val res = mutableListOf<Chunk>()
    for (match in matches) {

        val start = match.range.start
        val end = match.range.endInclusive + 1
        val startIndex = posIndexes.indexOf(start)
        val endIndex = if (end == i) posIndexes.size else posIndexes.indexOf(end)
        if (startIndex != -1 && endIndex != -1) {
            res.add(this.tokens().subList(startIndex, endIndex))
        }
    }

    return res
}
