package de.xeroli.kbool

import java.lang.IllegalArgumentException
import kotlin.streams.asSequence

open class Bool private constructor(protected val type: Type, protected var name: String = "") {

    enum class Type { BOOLEAN, SUPPLIER, AND, OR, NOT, XOR }

    protected var value = false
    protected var evaluated = false
    protected val entries = mutableSetOf<Entry>()

    protected data class Entry(val key: String, val value: Boolean) {
        override fun toString(): String {
            return "Entry('$key': $value)"
        }
    }

    class SupplierBool(val boolSupplier: () -> Bool) : Bool(Type.SUPPLIER, "") {

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

    class NotBool(private val inner: Bool) : Bool(Type.NOT, "") {

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

    class BinaryBool(type: Type, private val left: Bool, private val right: Bool) : Bool(type, "") {
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

    fun booleanValue(): Boolean {
        this.evaluate()
        return this.value
    }

    fun isTrue() = this.booleanValue()
    fun isFalse() = !this.booleanValue()

    fun getCause(separator: String = ", ", prefix: String = "", postfix: String = "", translator: (String) -> String = { s -> s }): String {
        this.evaluate()
        return this.entries.stream().map {
            "${translator.invoke(it.key)} - ${translator.invoke(it.value.toString())}"
        }.asSequence().joinToString(separator, prefix, postfix)
    }

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

    protected open fun evaluate() {
        if (!evaluated) {
            when (type) {
                Type.BOOLEAN -> evaluateBool()
                else -> throw IllegalArgumentException("$type is not supported")
            }
        }
    }

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

    operator fun not(): Bool {
        if (this.evaluated) {
            return Bool("", this.isFalse(), this.entries)
        }
        return NotBool(this)
    }

    override fun toString(): String {
        if (this.evaluated)
            return "EvaluatedBool(name=$name, value=$value, entries=$entries)"
        return "$type(name=$name)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bool
        if (toString() != other.toString()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + toString().hashCode()
        return result
    }

}

fun Boolean.asBool(name: String ="") = Bool.SupplierBool(this).named(name)
