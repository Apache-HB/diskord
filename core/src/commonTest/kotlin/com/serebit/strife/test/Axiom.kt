import AxiomSet.Axiom

/**
 * A Collections of Axioms which can be built in a easy to read fashion.
 *
 * Axiom: a statement or proposition on which an abstractly defined structure
 * is based.
 *
 * @param name The name of the [AxiomSet] (usually the name of the structure)
 * @param builder lambda for building type-safe and like a cool person B^)
 *
 *
 * ```kotlin
 *  val set = AxiomSet("SetName") {
 *      "AXIOM_NAME"("AXIOM_SPEC") {
 *          "CHILD_NAME"("CHILD_SPEC") {
 *              "CHILD_CHILD_NAME"("CHILD_CHILD_SPEC")
 *           }
 *          "CHILD_NAME_2"("CHILD_SPEC_2") {
 *              "CHILD_CHILD_NAME"("CHILD_CHILD_SPEC")
 *          }
 *      }
 *      "AXIOM_2"("AXIOM_SPEC_2")
 *      "AXIOM_3"("AXIOM_SPEC_3") {
 *          "AXIOM_3_CHILD"("AXIOM_3_CHILD_SPEC")
 *      }
 *  }
 * ```
 *
 * @author JonoAugustine (HQRegent)
 * @since 0.0.0
 */
class AxiomSet(
    val name: String,
    val axioms: MutableList<Axiom> = mutableListOf(),
    builder: AxiomSet.() -> AxiomSet = { this }
) : Iterable<Axiom> {

    init {
        builder(this)
    }

    data class Axiom(
        var name: String,
        var axiom: String,
        val parent: Axiom?,
        var example: String = ""
    ) : Iterable<Axiom> {
        val children = mutableListOf<Axiom>()

        operator fun String.invoke(
            axiomSpec: String,
            example: String = "",
            builder: Axiom.() -> Axiom = { this }
        ): Axiom {
            children.add(builder(Axiom(this, axiomSpec, this@Axiom, example)))
            return this@Axiom
        }

        fun example(example: String): Axiom {
            this.example = example
            return this
        }

        /**
         *      AXIOM_NAME: AXIOM_SPEC
         *          CHILD_NAME: CHILD_SPEC
         *              CHILD_CHILD_NAME: CHILD_CHILD_SPEC
         */
        override fun toString(): String {
            val sb = StringBuilder()
            fun printAxiomTree(axiom: Axiom, level: Int) {
                for (i in 0 until level) sb.append("\t")
                sb.append("${axiom.name}: ${axiom.axiom}\n")
                if (axiom.example.isNotBlank()) {
                    for (i in 0..level) sb.append("\t")
                    sb.append("${axiom.example}\n")
                }
                if (axiom.children.isNotEmpty())
                    axiom.forEach { printAxiomTree(it, level + 1) }
            }
            children.forEach {
                printAxiomTree(it, 0)
            }
            return sb.toString()
        }

        override fun iterator() = children.iterator()
    }

    operator fun invoke(b: AxiomSet.() -> Unit): AxiomSet {
        b(this)
        return this
    }

    operator fun String.invoke(
        axiomSpec: String,
        example: String = "",
        builder: Axiom.() -> Axiom = { this }
    ): AxiomSet {
        axioms.add(builder(Axiom(this, axiomSpec, null, example)))
        return this@AxiomSet
    }

    /**
     *      AXIOM_NAME: AXIOM_SPEC
     *          CHILD_NAME: CHILD_SPEC
     *              CHILD_CHILD_NAME: CHILD_CHILD_SPEC
     */
    override fun toString(): String {
        val sb = StringBuilder("$name\n")
        fun printAxiomTree(axiom: Axiom, level: Int) {
            for (i in 0 until level) sb.append("\t")
            sb.append("${axiom.name}: ${axiom.axiom}\n")
            if (axiom.example.isNotBlank()) {
                for (i in 0..level) sb.append("\t")
                sb.append("${axiom.example}\n")
            }
            if (axiom.children.isNotEmpty())
                axiom.forEach { printAxiomTree(it, level + 1) }
        }
        axioms.forEach {
            printAxiomTree(it, 0)
        }
        return sb.toString()
    }

    override fun iterator() = axioms.iterator()
}

fun main() {
    val set = AxiomSet("Axiom Set") {
        "Axiom Name"("this should do this", "example") {
            "sub-axiom"("sub-axiom should do this") {
                "sub-sub-axiom"("wow ok that's rather specific") {
                    example("sub-sub-example")
                }
            }
            "sub-axiom 2"("this sub-axiom is different", "sub-2 example")
        }
        "another axiom"("this is another main axiom") {
            example("anotherAxiom(juice: Meme) -> Joy?")
        }
        "a third axiom just to be safe"("idk anymore dude")
    }
    print(set)
}
