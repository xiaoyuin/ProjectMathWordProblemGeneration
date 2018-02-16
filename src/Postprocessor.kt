import edu.stanford.nlp.simple.Sentence

class Postprocessor {

    companion object {

        fun process(question: String, solution: String, sentences: List<String>): MathWordProblem {

            //TODO: Implement operations: corrects the grammar, adjusts the sentence order, recovers the subjects etc.
            val sentencesAfterOrdering = sentences.toMutableList()



            return MathWordProblem(sentencesAfterOrdering, question, solution)
        }

    }

}
