import edu.stanford.nlp.coref.CorefCoreAnnotations
import edu.stanford.nlp.coref.data.CorefChain
import edu.stanford.nlp.simple.Document
import edu.stanford.nlp.simple.Sentence
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File

import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefMentionsAnnotation
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.naturalli.Util.annotate
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.io.FileWriter
import java.util.*


class Preprocessor {

    private lateinit var process: Process
    private lateinit var writer: BufferedWriter
    private lateinit var reader: BufferedReader

    init {
        startSimplifier()
    }

    private fun startSimplifier() {
        process = Runtime.getRuntime()
                .exec("java -Xmx1500m -jar FactualStatementExtractor.jar")

        writer = process.outputStream.bufferedWriter()
        reader = process.inputStream.bufferedReader()
    }

    fun simplify(sentence: String): List<String> {

        writer.appendln(sentence)
        writer.flush()

        val simplified = reader.readLine()

        return Document(simplified).sentences().map { it.text() }
    }

    companion object {

        private var simplifierInstance: Preprocessor? = null

        fun process(input: String): List<List<String>> {

            // Split the input into sentences
            val document = Document(input)

            // Simplify the sentences
            val sentences = document.sentences()
            var result = mutableListOf<String>()
            for (sentence in sentences) {
                result.addAll(simplify(sentence.text()))
            }

            // Replace subjectives
            result = replaceSubjective(result.toList()).toMutableList()

            FileWriter("data/preprocessings.txt", true).run {
                appendln(result.join())
                close()
            }

            // Return the processed sentences
            return split(result)
        }

        private fun split(sentences: List<String>): List<List<String>> {

            val document = Document(sentences.join())

            val sentencesString = document.sentenceStrings()

            val res = mutableListOf<List<String>>()

            document.sentences().forEachIndexed { index, sentence ->
                if (sentence.nerTags().contains("NUMBER")) {
                    val copiedSentences = sentencesString.toMutableList()
                    copiedSentences.add(0, copiedSentences.removeAt(index))
                    res.add(copiedSentences)
                }
            }

            return res
        }

        private fun simplify(sentence: String): List<String> {

            if (simplifierInstance == null) {
                simplifierInstance = Preprocessor()
            }

            return simplifierInstance!!.simplify(sentence)
        }

        private fun replaceSubjective(sentences: List<String>): List<String> {

//            var document = Annotation(sentences.join())
//            val props = Properties()
//            props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref")
//            val pipeline = StanfordCoreNLP(props)
//            pipeline.annotate(document)
//            println("---")
//            println("coref chains")
//
//            var res = sentences
//
//            for (cc in document.get(CorefCoreAnnotations.CorefChainAnnotation::class.java).values) {
//                res = applyCorefChain(Document(res.join()), cc)
//            }
//
//            return res
            val document = Document(sentences.join())

            val corefMap = document.coref()
            var res = document.sentenceStrings()

            corefMap.forEach { key, corefChain ->
                val text = res.joinToString(" ")
                res = applyCorefChain(Document(text), corefChain)
            }

            return res
        }

        private fun applyCorefChain(document: Document, corefChain: CorefChain): List<String> {

            // Retrieve the person names from the co-reference chain
            val persons = corefChain.mentionsInTextualOrder
                    .filter { document.sentence(it.sentNum - 1).nerTags().subList(it.startIndex - 1, it.endIndex - 1).contains("PERSON") }
                    .map { it.mentionSpan }

            // If there is a person name, we replace all the other sentences containing co-reference with this name
            if (persons.isNotEmpty()) {
                val replacedSentences = document.sentences().mapIndexed { index, sentence ->
                    val corefMention = corefChain.mentionsInTextualOrder.find { index == it.sentNum - 1 }
                    if (corefMention != null && sentence.posTag(corefMention.startIndex-1) == "PRP") {
                        val copiedWords = sentence.words().toMutableList()
                        copiedWords[corefMention.startIndex -1] = persons[0]
                        return@mapIndexed copiedWords.joinToString(separator = " ")
                    } else {
                        return@mapIndexed sentence.text()
                    }
                }

                return replacedSentences
            } else {
                return document.sentenceStrings()
            }
        }

    }

}

fun List<String>.join(): String = this.joinToString(" ")

fun Document.sentenceStrings(): List<String> = this.sentences().map { it.text() }