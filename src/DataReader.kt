import edu.stanford.nlp.simple.Document
import java.io.File
import java.util.*
import javax.json.Json

fun main(args: Array<String>) {

    val questions = readQuestionsFromFile("data/AddSub.json")



    val outputFile = File("data/selection.txt")
    val writer = outputFile.bufferedWriter()

    val simpleQuestions = mutableListOf<Question>()
    val complexQuestions = mutableListOf<Question>()

    val threshold = 8

    questions.filter { it.question.contains("How many") }.forEach {
        val averageSentenceLength = Document(it.question).sentences().map { it.tokens().size }.average()
        if (averageSentenceLength > threshold) {
            complexQuestions.add(it)
        } else {
            simpleQuestions.add(it)
        }
    }

    listOf(simpleQuestions, complexQuestions).forEach {
        it.randomSelect(10).map { writer.appendln(it.question + " | " + it.solutions) }
    }

    writer.close()
}

fun readQuestionsFromFile(filePath: String): MutableList<Question> {
    val inputFile = File(filePath)

    val jsonReader = Json.createReader(inputFile.bufferedReader())

    val array = jsonReader.readArray()
    var result = mutableListOf<Question>()
    for (i in 0 until array.size) {
        val obj = array.getJsonObject(i)
        result.add(Question(obj.getString("sQuestion"),
                obj.getJsonArray("lSolutions").map { it.toString() },
                obj.getJsonArray("lEquations").map { it.toString() }))
    }
    jsonReader.close()

    return result
}

data class Question(var question: String, var solutions: List<String>, var equations: List<String>) {

}

fun List<Question>.randomSelect(num: Int): MutableList<Question> {
    val copy = this.toMutableList()
    if (num < 1) {
        throw IllegalArgumentException("The range of selection can not be: $num: meaningless")
    }
    if (num < copy.size) {
        val res = mutableListOf<Question>()
        for (i in 1..num) {
            val next = Random().nextInt(copy.size)
            res.add(copy.removeAt(next))
        }
        return res
    } else {
        println("Warning: Not enough data for selection, return all sources: required $num > size ${this.size}")
        return this.toMutableList()
    }
}