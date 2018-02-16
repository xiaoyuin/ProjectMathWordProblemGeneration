import edu.stanford.nlp.pipeline.StanfordCoreNLPServer
import edu.stanford.nlp.simple.Document
import java.io.File
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {

//    Runtime.getRuntime().exec("java -mx4g -cp \"*\" edu.stanford.nlp.pipeline.StanfordCoreNLPServer -port 9000 -timeout 15000")
//    Document.useServer("localhost", 9000)

    var countSuccess = 0
    var countProblems = 0
    var countFails = 0

    println("Please input the original problem:")
    val originalProblem = readLine()

    if (originalProblem == null){
        println("No input received. System terminates.")
        return
    }
    println()

    val totalTimeMills = measureTimeMillis {

        println("% Preprocessing for input ...")

        val list = Preprocessor.process(originalProblem)

        println("% Preprocessing for input DONE. ${list.size} new problems to be generated...")

        val result: MutableList<MathWordProblem> = mutableListOf()

        list.forEachIndexed { subIndex, preprocessedProblem ->

            if (preprocessedProblem.size >= 2) {

                print("% Generating problem #${ subIndex+1 }...")

                try {
                    // Generate the question solution pair for the postprocessing
                    val questionSolutionPair: Pair<String, String> = QuestionGenerator.generate(preprocessedProblem[0])

                    print("Postprocessing...")

                    // The generated problem consists of generated question solution pair and the rest of the statements
                    val newProblem = Postprocessor.process(questionSolutionPair.first, questionSolutionPair.second, preprocessedProblem.subList(1, preprocessedProblem.size))

                    result.add(newProblem)
                    println("DONE. ")
                } catch (err: IllegalStateException) {
                    println(err.message)
                }

            } else {

                // Dump the instances shorter than 2 sentences
                println("!! There is not enough information in this preprocessed problem." + preprocessedProblem)

            }

        }

        if (result.isNotEmpty()) {
            println("% Generated problems for input : ")
            result.forEach {
                println(it)
            }
            countSuccess += 1
            countProblems += result.size
        } else {
            println("% Generated problems for input FAILED.")
            println()
            countFails += 1
        }
    }

    println("% Results In total:")
    println("% $countSuccess problems succeeded / $countProblems new problems were generated")
    println("% $countFails problems failed")
    println("% Spent ${totalTimeMills / 1000 / 60} minutes ${totalTimeMills / 1000 % 60} seconds")
}