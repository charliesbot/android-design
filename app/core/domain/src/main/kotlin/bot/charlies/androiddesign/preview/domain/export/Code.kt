package bot.charlies.androiddesign.preview.domain.export

/**
 * A small model of the Kotlin the exporter writes, and the printer that lays it out the way ktfmt
 * (Google style) does: 2-space blocks, 100 columns, one argument per line with a trailing comma
 * once a call does not fit, and a break after `=` when the value does not fit on the line.
 */
internal sealed interface Expr {
  /** The expression on one line, or null when it always spans lines. */
  fun flat(): String?
}

/** Text that is never broken, such as a literal or a short call written out by hand. */
internal data class Raw(val text: String) : Expr {
  override fun flat() = text
}

/** `receiver.call().call()`. Each call is written whole, such as `padding(16.dp)`. */
internal data class Chain(val receiver: String, val calls: List<String>) : Expr {
  override fun flat() = receiver + calls.joinToString("") { ".$it" }
}

internal data class Arg(val name: String?, val value: Expr) {
  fun flat(): String? = value.flat()?.let { if (name == null) it else "$name = $it" }
}

/** `callee(args) { lambda }`. A call without arguments and without a lambda prints `callee()`. */
internal data class Call(
  val callee: String,
  val args: List<Arg> = emptyList(),
  val lambda: Lambda? = null,
) : Expr {
  override fun flat(): String? {
    val argText = args.map { it.flat() ?: return null }
    val parens = if (args.isEmpty() && lambda != null) "" else "(${argText.joinToString(", ")})"
    val trailing = if (lambda == null) "" else " " + (lambda.flat() ?: return null)
    return callee + parens + trailing
  }
}

internal data class Lambda(val params: String? = null, val body: List<Stmt> = emptyList()) : Expr {
  override fun flat(): String? {
    val head = if (params == null) "" else "$params -> "
    return when {
      body.isEmpty() -> if (params == null) "{}" else "{ $params -> }"
      body.size == 1 -> body.single().flat()?.let { "{ $head$it }" }
      else -> null
    }
  }
}

/** `if (condition) { { ... } } else null`, always on several lines like ktfmt writes it. */
internal data class IfValue(val condition: String, val whenTrue: Lambda, val otherwise: String) :
  Expr {
  override fun flat(): String? = null
}

internal sealed interface Stmt {
  fun flat(): String?
}

internal data class ExprStmt(val expr: Expr) : Stmt {
  override fun flat() = expr.flat()
}

/** `val x = value`, `x = value` or `var x by value`; [lhs] includes the keyword and name. */
internal data class Assign(val lhs: String, val op: String, val rhs: Expr) : Stmt {
  override fun flat(): String? = rhs.flat()?.let { "$lhs $op $it" }
}

internal data class IfStmt(val branches: List<Pair<String, List<Stmt>>>, val orElse: List<Stmt>?) :
  Stmt {
  override fun flat(): String? = null
}

internal fun arg(name: String, value: Expr) = Arg(name, value)

internal fun arg(name: String, value: String) = Arg(name, Raw(value))

internal fun pos(value: Expr) = Arg(null, value)

internal fun pos(value: String) = Arg(null, Raw(value))

internal fun stmt(expr: Expr): Stmt = ExprStmt(expr)

internal fun lambda(body: List<Stmt>, params: String? = null) = Lambda(params, body)

internal fun lambda(vararg body: Stmt) = Lambda(null, body.toList())

internal fun lambdaOf(expr: Expr) = Lambda(null, listOf(ExprStmt(expr)))

/** A Kotlin string literal for [value]. */
internal fun kotlinString(value: String): String = buildString {
  append('"')
  for (c in value) {
    when {
      c == '\\' -> append("\\\\")
      c == '"' -> append("\\\"")
      c == '$' -> append("\\$")
      c == '\n' -> append("\\n")
      c == '\r' -> append("\\r")
      c == '\t' -> append("\\t")
      c < ' ' -> append("\\u%04x".format(c.code))
      else -> append(c)
    }
  }
  append('"')
}

/** Top-level declarations. */
internal sealed interface Decl

internal data class FunDecl(
  val annotations: List<String>,
  val modifiers: String,
  val name: String,
  val params: List<String>,
  val body: List<Stmt>,
) : Decl

/** A top-level or member property: `private val name = value`. */
internal data class ValDecl(val modifiers: String, val name: String, val value: Expr) : Decl

internal data class ClassDecl(
  val annotations: List<String>,
  val header: String,
  val fields: List<String>,
) : Decl

internal data class ObjectDecl(
  val modifiers: String,
  val name: String,
  val members: List<ValDecl>,
) : Decl

/** Lines written exactly as given (comments, `@file:` annotations, helper functions). */
internal data class RawDecl(val lines: List<String>) : Decl

internal object CodePrinter {
  const val WIDTH = 100
  private const val INDENT = 2

  fun print(decls: List<Decl>): String =
    decls.joinToString("\n\n") { decl -> printDecl(decl).joinToString("\n") } + "\n"

  fun printStatements(stmts: List<Stmt>, indent: Int = 0): List<String> {
    val out = mutableListOf<String>()
    stmts.forEach { printStmt(it, indent, out) }
    return out
  }

  private fun printDecl(decl: Decl): List<String> {
    val out = mutableListOf<String>()
    when (decl) {
      is RawDecl -> out += decl.lines
      is ValDecl -> printWithPrefix("${decl.modifiers}val ${decl.name} =", decl.value, "", 0, out)
      is ClassDecl -> printClass(decl, out)
      is ObjectDecl -> {
        out += "${decl.modifiers}object ${decl.name} {"
        decl.members.forEach {
          printWithPrefix("${it.modifiers}val ${it.name} =", it.value, "", INDENT, out)
        }
        out += "}"
      }
      is FunDecl -> printFun(decl, out)
    }
    return out
  }

  private fun printClass(decl: ClassDecl, out: MutableList<String>) {
    val annotations =
      decl.annotations.joinToString(" ") + if (decl.annotations.isEmpty()) "" else " "
    val flat = "$annotations${decl.header}(${decl.fields.joinToString(", ")})"
    if (decl.fields.isEmpty() || flat.length <= WIDTH) {
      out += if (decl.fields.isEmpty()) "$annotations${decl.header}" else flat
    } else {
      out += "$annotations${decl.header}("
      decl.fields.forEach { out += "${pad(INDENT)}$it," }
      out += ")"
    }
  }

  private fun printFun(decl: FunDecl, out: MutableList<String>) {
    decl.annotations.forEach { out += it }
    val flat = "${decl.modifiers}fun ${decl.name}(${decl.params.joinToString(", ")}) {"
    if (flat.length <= WIDTH) {
      out += flat
    } else {
      out += "${decl.modifiers}fun ${decl.name}("
      decl.params.forEach { out += "${pad(INDENT)}$it," }
      out += ") {"
    }
    decl.body.forEach { printStmt(it, INDENT, out) }
    out += "}"
  }

  private fun printStmt(stmt: Stmt, indent: Int, out: MutableList<String>) {
    when (stmt) {
      is ExprStmt -> printWithPrefix("", stmt.expr, "", indent, out)
      is Assign -> printWithPrefix("${stmt.lhs} ${stmt.op}", stmt.rhs, "", indent, out)
      is IfStmt -> {
        stmt.branches.forEachIndexed { i, (condition, body) ->
          out += "${pad(indent)}${if (i == 0) "" else "} else "}if ($condition) {"
          body.forEach { printStmt(it, indent + INDENT, out) }
        }
        if (stmt.orElse != null) {
          out += "${pad(indent)}} else {"
          stmt.orElse.forEach { printStmt(it, indent + INDENT, out) }
        }
        out += "${pad(indent)}}"
      }
    }
  }

  /**
   * Prints [expr] after [prefix] (`val x =`, `name =` or nothing) with [suffix] (a comma) after it.
   * Mirrors ktfmt: the whole thing on one line when it fits, else a break after `=` for any value
   * but a lambda, else the call's own break.
   */
  private fun printWithPrefix(
    prefix: String,
    expr: Expr,
    suffix: String,
    indent: Int,
    out: MutableList<String>,
  ) {
    val head = if (prefix.isEmpty()) "" else "$prefix "
    val flat = expr.flat()
    if (flat != null && indent + head.length + flat.length + suffix.length <= WIDTH) {
      out += "${pad(indent)}$head$flat$suffix"
      return
    }
    if (prefix.endsWith("=") && expr !is Lambda) {
      out += "${pad(indent)}$prefix"
      printWithPrefix("", expr, suffix, indent + INDENT, out)
      return
    }
    when (expr) {
      is Raw -> printRaw(head, expr.text, suffix, indent, out)
      is Chain -> printChain(head, expr, suffix, indent, out)
      is Call -> printCall(head, expr, suffix, indent, out)
      is Lambda -> {
        out += "${pad(indent)}$head{${expr.params?.let { " $it ->" } ?: ""}"
        expr.body.forEach { printStmt(it, indent + INDENT, out) }
        out += "${pad(indent)}}$suffix"
      }
      is IfValue -> {
        out += "${pad(indent)}${head}if (${expr.condition}) {"
        printWithPrefix("", expr.whenTrue, "", indent + INDENT, out)
        out += "${pad(indent)}} else ${expr.otherwise}$suffix"
      }
    }
  }

  /**
   * Hand-written text that is too long for its line, broken the way ktfmt breaks it: a call puts
   * each argument on its own line with a trailing comma, and an `if (c) a else b` puts the `else`
   * on a new line. Text that fits, or has a shape this does not know, is written as it is.
   */
  private fun printRaw(
    head: String,
    text: String,
    suffix: String,
    indent: Int,
    out: MutableList<String>,
  ) {
    val line = "${pad(indent)}$head$text$suffix"
    if (line.length <= WIDTH || '{' in text) {
      out += line
      return
    }
    ifElseParts(text)?.let { (thenPart, elsePart) ->
      out += "${pad(indent)}$head$thenPart"
      out += "${pad(indent)}$elsePart$suffix"
      return
    }
    val open = text.indexOf('(')
    if (open < 0 || !text.endsWith(")") || matchingParen(text, open) != text.lastIndex) {
      out += line
      return
    }
    out += "${pad(indent)}$head${text.substring(0, open + 1)}"
    topLevelArguments(text.substring(open + 1, text.lastIndex)).forEach {
      printRaw("", it, ",", indent + INDENT, out)
    }
    out += "${pad(indent)})$suffix"
  }

  /** `if (c) a else b` as (`if (c) a`, `else b`), or null for any other text. */
  private fun ifElseParts(text: String): Pair<String, String>? {
    if (!text.startsWith("if (")) return null
    val afterCondition = matchingParen(text, 3) + 1
    val elseAt = Regex(" else ").find(text, afterCondition)?.range?.first ?: return null
    return text.substring(0, elseAt) to text.substring(elseAt + 1)
  }

  /** The index of the `)` that closes the `(` at [open], skipping string literals. */
  private fun matchingParen(text: String, open: Int): Int {
    var depth = 0
    var inString = false
    var i = open
    while (i < text.length) {
      val c = text[i]
      when {
        inString && c == '\\' -> i++
        c == '"' -> inString = !inString
        inString -> {}
        c == '(' -> depth++
        c == ')' -> if (--depth == 0) return i
      }
      i++
    }
    return -1
  }

  /** [text] split at the [separator]s that are not inside brackets or string literals. */
  private fun topLevelArguments(text: String, separator: Char = ','): List<String> {
    val parts = mutableListOf<String>()
    var depth = 0
    var inString = false
    var start = 0
    var i = 0
    while (i < text.length) {
      val c = text[i]
      when {
        inString && c == '\\' -> i++
        c == '"' -> inString = !inString
        inString -> {}
        c == '(' || c == '[' || c == '{' -> depth++
        c == ')' || c == ']' || c == '}' -> depth--
        c == separator && depth == 0 -> {
          parts += text.substring(start, i).trim()
          start = i + 1
        }
      }
      i++
    }
    parts += text.substring(start).trim()
    return parts.filter { it.isNotEmpty() }
  }

  private fun printChain(
    head: String,
    chain: Chain,
    suffix: String,
    indent: Int,
    out: MutableList<String>,
  ) {
    val attach = chain.receiver.firstOrNull()?.isUpperCase() == true && chain.calls.isNotEmpty()
    val first = head + chain.receiver + if (attach) ".${chain.calls.first()}" else ""
    val rest = if (attach) chain.calls.drop(1) else chain.calls
    if (rest.isEmpty()) {
      printRaw("", first, suffix, indent, out)
      return
    }
    val last = rest.singleOrNull()
    if (attach && last != null && last.endsWith(" }") && last.contains(" { ")) {
      // ktfmt keeps `Modifier.a().b {` together and opens the block on that line.
      out += "${pad(indent)}$first.${last.substringBefore(" { ")} {"
      out += "${pad(indent + INDENT)}${last.substringAfter(" { ").removeSuffix(" }")}"
      out += "${pad(indent)}}$suffix"
      return
    }
    out += "${pad(indent)}$first"
    rest.forEachIndexed { i, call ->
      printChainCall(call, if (i == rest.lastIndex) suffix else "", indent + INDENT, out)
    }
  }

  /**
   * `.call(...)` on one line, or one argument per line with a trailing comma when it is too long.
   */
  private fun printChainCall(call: String, suffix: String, indent: Int, out: MutableList<String>) {
    val line = "${pad(indent)}.$call$suffix"
    val open = call.indexOf('(')
    val args =
      if (line.length > WIDTH && open > 0 && call.endsWith(")")) {
        topLevelArguments(call.substring(open + 1, call.length - 1))
      } else {
        null
      }
    if (args == null || args.size < 2) {
      out += line
      return
    }
    out += "${pad(indent)}.${call.substring(0, open)}("
    args.forEach { printChainArg(it, indent + INDENT, out) }
    out += "${pad(indent)})$suffix"
  }

  /**
   * One argument of a wrapped chain call, with a lambda of several statements opened over lines:
   * `onClick = { a; b }` becomes a block of `a` and `b`.
   */
  private fun printChainArg(arg: String, indent: Int, out: MutableList<String>) {
    val head = arg.substringBefore("{", "")
    val body = arg.substringAfter("{", "").removeSuffix("}").trim()
    val statements =
      if (head.endsWith("= ") && arg.endsWith("}")) topLevelArguments(body, ';') else emptyList()
    if (statements.size < 2) {
      out += "${pad(indent)}$arg,"
      return
    }
    out += "${pad(indent)}${head}{"
    statements.forEach { out += "${pad(indent + INDENT)}$it" }
    out += "${pad(indent)}},"
  }

  private fun printCall(
    head: String,
    call: Call,
    suffix: String,
    indent: Int,
    out: MutableList<String>,
  ) {
    val lambda = call.lambda
    if (lambda == null) {
      if (call.args.isEmpty()) {
        out += "${pad(indent)}$head${call.callee}()$suffix"
        return
      }
      out += "${pad(indent)}$head${call.callee}("
      printArgs(call.args, indent, out)
      out += "${pad(indent)})$suffix"
      return
    }
    val open = " {${lambda.params?.let { " $it ->" } ?: ""}"
    val argText = call.args.map { it.flat() }
    val inlineArgs =
      if (call.args.isEmpty()) ""
      else if (argText.all { it != null }) "(${argText.joinToString(", ")})" else null
    if (
      inlineArgs != null &&
        indent + head.length + call.callee.length + inlineArgs.length + open.length <= WIDTH
    ) {
      out += "${pad(indent)}$head${call.callee}$inlineArgs$open"
    } else {
      out += "${pad(indent)}$head${call.callee}("
      printArgs(call.args, indent, out)
      out += "${pad(indent)})$open"
    }
    lambda.body.forEach { printStmt(it, indent + INDENT, out) }
    out += "${pad(indent)}}$suffix"
  }

  private fun printArgs(args: List<Arg>, indent: Int, out: MutableList<String>) {
    args.forEach { arg ->
      printWithPrefix(arg.name?.let { "$it =" } ?: "", arg.value, ",", indent + INDENT, out)
    }
  }

  private fun pad(indent: Int) = " ".repeat(indent)
}
