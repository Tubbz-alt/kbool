package de.xeroli.kbool

/**
 * isTrue()
 * returns true if booleanValue() returns true
 */
fun Bool.isTrue() = this.booleanValue()

/**
 * isFalse()
 * returns true if booleanValue() returns false
 */
fun Bool.isFalse() = !this.booleanValue()

/**
 * extends Boolean to convert it into an Bool
 */
fun Boolean.asBool(name: String = ""): Bool = EvaluatedBool(this).named(name)
