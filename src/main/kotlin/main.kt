package calculator

class MutableStack<E>(vararg items: E) {
    private val elements = items.toMutableList()
    fun push(element: E) = elements.add(element)
    fun peek(): E = elements.last()
    fun pop(): E = elements.removeAt(elements.size - 1)
    fun isEmpty() = elements.isEmpty()
    fun size() = elements.size
    override fun toString() = "MutableStack(${elements.joinToString()})"
}

class SmartCalculator {
    private val vars = mutableMapOf<String, String>()

    private fun getMatches(expr: Regex, text: String): MutableList<String> {
        val res = mutableListOf<String>()
        for (el in expr.findAll(text)) {
            res.add(el.value)
        }
        return res
    }

    private fun replaceSignsAndVars(inpLst: MutableList<String>): Boolean {
        val pluses = """[+]{2,}""".toRegex() // 2 or more "+"
        val minuses = """-{2,}""".toRegex() // 2 or more "-"
        val plusMinus = """[+][-]|[-][+]""".toRegex()
        val isVariable = """[a-zA-Z]+""".toRegex()
        val invalidSign = """[*/^]{2,}""".toRegex()

        for (i in inpLst.indices) {
            if (invalidSign.matches(inpLst[i])) {
                println("Invalid expression")
                return false
            }
            if (isVariable.matches(inpLst[i])) {
                if (inpLst[i] !in vars) {
                    println("Unknown variable")
                    return false
                }
                inpLst[i] = vars[inpLst[i]]!!
            }
            if (pluses.matches(inpLst[i])) {
                inpLst[i] = "+"
            }
            if (minuses.matches(inpLst[i])) {
                if (inpLst[i].length % 2 == 0) {
                    inpLst[i] = "+"
                } else {
                    inpLst[i] = "-"
                }
            }
            if (plusMinus.containsMatchIn(inpLst[i])) {
                inpLst[i] = inpLst[i].replace(plusMinus, "-")
            }
        }
        return true
    }

    private fun getHelp() {
        println("The program calculates the sum of numbers")
        println("Even number of minuses gives a plus, and the" +
                " odd number of minuses gives a minus")
        println("Available operators: multiplication *, integer " +
                "division /, parentheses (...) and power of ^")
    }

    private fun checkBrackets(elems: List<String>): Boolean {
        val st = MutableStack<String>()

        for (el in elems) {
            if (el == "(") {
                st.push(el)
            } else if (el == ")") {
                if (!st.isEmpty()) { // ")1 + 2" or "(1 + 2))"
                    st.pop()
                } else return false
            }
        }
        return st.isEmpty()
    }

    private fun addVariable(inp: String): Boolean {
        val toConv = """[a-zA-Z]+""".toRegex()
        val inpNoSpaces = inp.replace(" ", "")
        val inpArr = inpNoSpaces.split("=")

        if (toConv.matches(inpArr[1])) { // right-hand operator is a variable
            return if (inpArr[1] in vars) {
                vars[inpArr[0]] = vars[inpArr[1]] ?: ""
                true
            } else {
                println("Unknown variable")
                false
            }
        }
        vars[inpArr[0]] = inpArr[1]
        return true
    }

    private fun infixToPostfix(inpLst: MutableList<String>): MutableList<String> {
        val preced = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2, "^" to 3)
        val num = """-?\d+""".toRegex()
        val postfix = mutableListOf<String>()
        val st = MutableStack<String>()

        for (el in inpLst) {
            when {
                el == "(" -> st.push(el)
                el == ")" -> {
                    while (st.peek() != "(") {
                        postfix.add(st.pop())
                    }
                    st.pop()
                }
                num.matches(el) -> {
                    postfix.add(el)
                }
                el in preced -> {
                    val prec = preced[el]!!
                    while (!st.isEmpty()) {
                        if (st.peek() in preced && (preced[st.peek()] ?: 0) >= prec) {
                            postfix.add(st.pop())
                        } else break
                    }
                    st.push(el)
                }
            }
        }
        while (!st.isEmpty()) {
            postfix.add(st.pop())
        }
        return postfix
    }

    private fun calculate(inpLst: MutableList<String>) {
        val toCalc = infixToPostfix(inpLst)
        val st = MutableStack<String>()

        for (el in toCalc) {
            when (el) {
                "+" -> {
                    val n2 = st.pop().toBigInteger()
                    val n1 = st.pop().toBigInteger()
                    st.push((n1 + n2).toString())
                }
                "-" -> {
                    val n2 = st.pop().toBigInteger()
                    val n1 = st.pop().toBigInteger()
                    st.push((n1 - n2).toString())
                }
                "*" -> {
                    val n2 = st.pop().toBigInteger()
                    val n1 = st.pop().toBigInteger()
                    st.push((n1 * n2).toString())
                }
                "/" -> {
                    val n2 = st.pop().toBigInteger()
                    val n1 = st.pop().toBigInteger()
                    st.push((n1 / n2).toString())
                }
                "^" -> {
                    val n2 = st.pop().toInt()
                    val n1 = st.pop().toInt()
                    if (n1 == 0) println("Invalid expression")
                    else if (n2 == 0) {
                        val num = """\d+""".toRegex()
                        st.push(n1.toString().replace(num, "1"))
                    } else {
                        var res = 1
                        repeat(n2) {
                            res *= n1
                        }
                        st.push(res.toString())
                    }
                }
                else -> {
                    st.push(el)
                }
            }
        }
        println(st.pop())
    }

    fun runCalc() {
        val commd = """/(help|exit)""".toRegex() // valid command
        val variables = """^[a-zA-Z]+ *= *[+-]*(\d+|[a-zA-Z]+)$""".toRegex()
        val invIdent = """([a-zA-Z]+\d+.*|\d+[a-zA-Z]+.*) *=.*""".toRegex()
        val invAssig = """[a-zA-Z]+ *= *([a-zA-Z].*\d+.*|\d+[a-zA-Z].*|.*=.*)""".toRegex()
        val elements = """((?<=^)[-+]+\d+|[a-zA-Z]+)|\d+|[a-zA-Z]+|[-+]+|[/*^]+|[()]""".toRegex() // operators and operands
        val oneNum = """[-+]?\d+""".toRegex()
        val invalidOneNum = """[-+]+ +\d+|[/*^]+ *\d+|[-+]{2,} *\d+""".toRegex()
        val opersInvalid = """[-+*/]+""".toRegex()

        while (true) {
            val inp = readln().trim()

            when {
                inp.startsWith("/") -> { // it is a command
                    if (!commd.matches(inp)) {
                        println("Unknown command")
                    } else {
                        when (inp) {
                            "/exit" -> {
                                println("Bye!")
                                break
                            }
                            "/help" -> getHelp()
                        }
                    }
                }
                inp == "" -> continue
                variables.matches(inp) -> {
                    addVariable(inp)
                }
                invIdent.matches(inp) -> { // invalid identifier
                    println("Invalid identifier")
                }
                invAssig.matches(inp) -> { // invalid assignment
                    println("Invalid assignment")
                }
                oneNum.matches(inp) -> {
                    println(inp.toInt())
                }
                invalidOneNum.matches(inp) || opersInvalid.matches(inp) -> {
                    println("Invalid expression")
                }
                else -> {
                    val toCalc = getMatches(elements, inp)
                    if (!checkBrackets(toCalc)) {
                        println("Invalid expression")
                        continue
                    }
                    if (!replaceSignsAndVars(toCalc)) {
                        continue
                    }
                    calculate(toCalc)
                }
            }
        }
    }
}

fun main() {
    val calc = SmartCalculator()
    calc.runCalc()
}