import edu.stanford.nlp.simple.Sentence

class Postprocessor {

    companion object {

        fun process(question: String, solution: String, sentences: List<String>): MathWordProblem {

            val sentencesAfterOrdering = sentences.toMutableList()

            val indexOfFirstSentenceWithNumber = sentences.indexOfFirst {
                Sentence(it).nerTags().contains("NUMBER")
            }

            if (indexOfFirstSentenceWithNumber != -1) {
                sentencesAfterOrdering.add(0, sentencesAfterOrdering.removeAt(indexOfFirstSentenceWithNumber))
            }

            return MathWordProblem(sentencesAfterOrdering, question, solution)
        }

    }

}
