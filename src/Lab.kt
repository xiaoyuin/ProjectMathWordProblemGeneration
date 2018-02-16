import edu.stanford.nlp.simple.Document
import edu.stanford.nlp.simple.Sentence

fun main(args: Array<String>) {

//    println(sentence.parse())
//    println(sentence.posTags())
//    println(sentence.nerTags())


//    val annotation = Annotation("Joan found 70 seashells on the beach . she gave Sam some of her seashells . She has 27 seashell . How many seashells did she give to Sam ? ")
//
//    annotation.get(CoreAnnotations.SentencesAnnotation::class.java)
//
//
    val text = "Joan and Tom are friends. They have 48 books together."


//    val props = Properties()
//    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse, coref")
//
//    val pipeline = StanfordCoreNLP(props)
//
//
//    val document = Annotation(sentence)
//
//    pipeline.annotate(document)

//    val document = Document(text)
//
//    val sentence = Sentence(text)

//
//    println(sentence.dependencyGraph())
//    println(sentence.posTags())
//    println(sentence.parse())
//    println(sentence.lemmas())
//

//    val generatedQuestion = QuestionGenerator.generate(text)
//
//    println(generatedQuestion)

    println(Preprocessor.process(text))

//    println(Document(text).coref())

}