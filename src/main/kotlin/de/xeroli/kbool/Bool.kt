package de.xeroli.kbool

import java.lang.IllegalArgumentException
import kotlin.streams.asSequence

/**
 * Bool - a simple class providing a transparent boolean algebra
 * for usage see https://github.com/xeroli/kbool
 */
open class Bool private constructor(internal val type: Type, internal var name: String = "") {

    internal enum class Type { BOOLEAN, SUPPLIER, AND, OR, NOT, XOR }

    internal var value = false
    internal var evaluated = false
    internal val entries = mutableSetOf<Entry>()

    internal data class Entry(val key: String, val value: Boolean) {
        override fun toString(): String {
            return "Entry('$key': $value)"
        }
    }

    companion object Factory {
        fun of(supplier: () -> Boolean): Bool {
            return SupplierBool { SupplierBool(supplier.invoke()) }
        }

        fun of(bool: Boolean): Bool {
            return bool.asBool()
        }

    }

    internal class SupplierBool(val boolSupplier: () -> Bool) : Bool(Type.SUPPLIER, "") {

        constructor(bool: Boolean) : this({ Bool("", bool) })

        override fun evaluate() {
            val innerBool = this.boolSupplier().named(this.name)
            innerBool.evaluate()
            this.value = innerBool.value
            if (this.name.isBlank())
                this.name = innerBool.name
            this.entries.clear()
            if (this.name.isNotBlank()) {
                this.entries.add(Entry(name, this.value))
            } else {
                this.entries.addAll(innerBool.entries)
            }
            this.evaluated = true
        }

        override fun toString(): String {
            if (this.evaluated)
                return super.toString()
            return "$type(name=$name, supplierHash=#${boolSupplier.hashCode()})"
        }

    }

    internal class NotBool(private val inner: Bool) : Bool(Type.NOT, "") {

        override fun evaluate() {
            inner.evaluate()
            this.value = !inner.value
            this.entries.clear()
            if (this.name.isBlank()) {
                this.entries.addAll(inner.entries)
            } else {
                this.entries.add(Entry(this.name, this.value))
            }
            this.evaluated = true
        }

        override fun toString(): String {
            if (this.evaluated)
                return super.toString()
            return "$type(name=$name, $inner)"
        }

    }

    internal class BinaryBool internal constructor(type: Type, private val left: Bool, private val right: Bool) : Bool(type, "") {
        private fun copyFrom(other: Bool) {
            this.value = other.value
            this.entries.clear()
            if (this.name.isBlank()) {
                this.entries.addAll(other.entries)
            } else {
                this.entries.add(Entry(this.name, this.value))
            }
            this.evaluated = true
        }

        override fun evaluate() {
            left.evaluate()
            when (type) {
                Type.AND -> if (!left.value) {
                    copyFrom(left); return
                }
                Type.OR -> if (left.value) {
                    copyFrom(left); return
                }
                else -> {
                }
            }
            right.evaluate()
            when (type) {
                Type.AND -> if (!right.value) {
                    copyFrom(right); return
                }
                Type.OR -> if (right.value) {
                    copyFrom(right); return
                }
                else -> {
                }
            }
            this.value = when (type) {
                Type.AND -> left.value and right.value
                Type.OR -> left.value or right.value
                Type.XOR -> left.value xor right.value
                else -> false
            }
            this.entries.clear()
            if (this.name.isBlank()) {
                this.entries.addAll(left.entries)
                this.entries.addAll(right.entries)
            } else {
                this.entries.add(Entry(this.name, this.value))
            }
            this.evaluated = true
        }

        override fun toString(): String {
            if (this.evaluated)
                return super.toString()
            return "$type(name=$name, left=$left, right=$right)"
        }

    }

    internal constructor(name: String, bool: Boolean) : this(Type.BOOLEAN, name) {
        this.value = bool
    }

    private constructor(name: String, value: Boolean, entries: Set<Entry>) : this(Type.BOOLEAN, name) {
        this.entries.addAll(entries)
        this.value = value
        this.evaluated = true
    }

    /**
     * booleanValue()
     * returns the booleanValuu of the evaluated Bool.
     * If the Bool isn't evaluated yet, the evaluation ist forced.
     */
    fun booleanValue(): Boolean {
        this.evaluate()
        return this.value
    }

    /**
     * isTrue()
     * returns true if booleanValue() returns true
     */
    fun isTrue() = this.booleanValue()

    /**
     * isFalse()
     * returns true if booleanValue() returns false
     */
    fun isFalse() = !this.booleanValue()

    /**
     * getCause()
     * returns the cause of an evaluated Bool as String. If the Bool isn't ebvaluated yet, evaluation is forced.
     * The parameters may change in the near future ....
     */
    fun getCause(separator: String = ", ", prefix: String = "", postfix: String = "", translator: (String) -> String = { s -> s }): String {
        this.evaluate()
        return this.entries.stream().map {
            "${translator.invoke(it.key)} - ${translator.invoke(it.value.toString())}"
        }.asSequence().joinToString(separator, prefix, postfix)
    }

    /**
     * named()
     * set the name of this boolean, overwrites an existing name without warning
     */
    fun named(newName: String): Bool {
        if (newName.isNotBlank() and this.evaluated) {
            this.entries.clear()
            this.entries.add(Entry(newName, this.value))
        }
        this.name = newName
        return this
    }

    private fun evaluateBool() {
        if (this.name.isNotBlank()) {
            this.entries.clear()
            this.entries.add(Entry(name, this.value))
        }
        this.evaluated = true
    }

    internal open fun evaluate() {
        if (!evaluated) {
            when (type) {
                Type.BOOLEAN -> evaluateBool()
                else -> throw IllegalArgumentException("$type is not supported")
            }
        }
    }

    /**
     * and()
     * returns a Bool representing the value of (left and right)
     * if evaluation is possible, an evaluated Bool will be returned
     */
    infix fun and(other: Bool): Bool {
        if (this.evaluated) {
            if (this.isFalse())
                return this
            if (other.evaluated) {
                if (other.isFalse())
                    return other
            }
        }
        return if (this.evaluated && other.evaluated) {
            val entries = this.entries
            entries.addAll(other.entries)
            Bool("", true, entries)
        } else {
            val result: Bool = BinaryBool(Type.AND, this, other)
            result
        }
    }

    /**
     * or()
     * returns a Bool representing the value of (left or right)
     * if evaluation is possible, an evaluated Bool will be returned
     */
    infix fun or(other: Bool): Bool {
        if (this.evaluated) {
            if (this.isTrue())
                return this
            if (other.evaluated) {
                if (other.isTrue())
                    return other
            }
        }
        return if (this.evaluated && other.evaluated) {
            val entries = this.entries
            entries.addAll(other.entries)
            Bool("", false, entries)
        } else {
            BinaryBool(Type.OR, this, other)
        }
    }

    /**
     * not()
     * returns the negation of the Bool.
     * if it was evaluated, the resultin Bool ist evaluated too.
     * if not, a new not-evaluated Bool will be returned
     */
    operator fun not(): Bool {
        if (this.evaluated) {
            return Bool("", this.isFalse(), this.entries)
        }
        return NotBool(this)
    }

    /**
     * toString()
     * produces an unique value for Bool instances, that are not evaluated.
     * As soon as a Bool ist evaluated, only value and cause are relevant.
     */
    override fun toString(): String {
        if (this.evaluated)
            return "EvaluatedBool(name=$name, value=$value, entries=$entries)"
        return "$type(name=$name)"
    }

    /**
     * equals()
     * two Bool instances are equal, ist their String respresentation ist equal
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bool
        if (toString() != other.toString()) return false

        return true
    }

    /**
     * hashCode()
     * returns a modified hashCode of the result of toString()
     */
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + toString().hashCode()
        return result
    }
}

fun Boolean.asBool(name: String = "") = Bool.SupplierBool(this).named(name)
