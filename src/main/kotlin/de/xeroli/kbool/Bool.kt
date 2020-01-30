/*
 * Copyright 2020 Roland Fischer (fischer@xeroli.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package de.xeroli.kbool

import kotlin.IllegalArgumentException
import kotlin.streams.asSequence

/**
 * Bool - a simple class providing a transparent boolean algebra
 * for usage see https://github.com/xeroli/kbool
 */
abstract class Bool private constructor(internal val type: Type) {

    internal enum class Type { BOOLEAN, SUPPLIER, AND, OR, NOT, XOR }

    internal var name: String = ""

    companion object Factory {
        /**
         * Bool.of( ()->Boolean ) - factory method to create an Bool with late evaluation
         */
        fun of(supplier: () -> Boolean): Bool = SupplierBool { SupplierBool(supplier.invoke()) }

        /**
         * Bool.of( Boolean ) - factory method to create an Bool with direct evaluation
         */
        fun of(bool: Boolean): Bool = EvaluatedBool(bool)
    }

    internal class EvaluatedBool(internal var value: Boolean, internal val entries: MutableSet<Entry> = mutableSetOf(Entry(value.toString().toUpperCase(), value))) : Bool(Type.BOOLEAN) {

        data class Entry(val key: String, val value: Boolean) {
            override fun toString() = "Entry('$key': $value)"
        }

        override fun evaluate() = this
        override fun toString() = "$type(name='$name', value=$value, entries=$entries)"

        internal fun rename(newName: String): EvaluatedBool {
            if (newName.isNotBlank()) {
                this.named(newName)
            }
            return this
        }

        internal fun toggle(): EvaluatedBool {
            this.value = !this.value
            return this
        }
    }

    internal class SupplierBool(private val boolSupplier: () -> Bool) : Bool(Type.SUPPLIER) {
        constructor(bool: Boolean) : this({ EvaluatedBool(bool) })

        override fun evaluate() = this.boolSupplier().evaluate().rename(this.name)
        override fun toString() = "$type(name='$name', supplierHash=#${boolSupplier.hashCode()})"
    }

    internal class NotBool(private val inner: Bool) : Bool(Type.NOT) {
        override fun evaluate() = this.inner.evaluate().toggle().rename(this.name)
        override fun toString() = "$type(name='$name', $inner)"
    }

    internal class BinaryBool internal constructor(type: Type, private val left: Bool, private val right: Bool) : Bool(type) {
        override fun evaluate(): EvaluatedBool {
            val innerLeft = left.evaluate()
            when (type) {
                Type.AND -> if (!innerLeft.value) return innerLeft.rename(this.name)
                Type.OR -> if (innerLeft.value) return innerLeft.rename(this.name)
                else -> {
                }
            }
            val innerRight = right.evaluate()
            when (type) {
                Type.AND -> if (!innerRight.value) return innerRight.rename(this.name)
                Type.OR -> if (innerRight.value) return innerRight.rename(this.name)
                else -> {
                }
            }
            val innerValue = when (type) {
                Type.AND -> innerLeft.value and innerRight.value
                Type.OR -> innerLeft.value or innerRight.value
                Type.XOR -> innerLeft.value xor innerRight.value
                else -> throw IllegalArgumentException("unknown type")
            }
            val innerEntries = mutableSetOf<EvaluatedBool.Entry>()
            if (this.name.isBlank()) {
                innerEntries.addAll(innerLeft.entries)
                innerEntries.addAll(innerRight.entries)
            }
            return EvaluatedBool(innerValue, innerEntries).rename(this.name)
        }

        override fun toString() = "$type(name='$name', left=$left, right=$right)"
    }

    /**
     * booleanValue()
     * returns the booleanValuu of the evaluated Bool.
     * If the Bool isn't evaluated yet, the evaluation ist forced.
     */
    fun booleanValue() = this.evaluate().value

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
     */
    fun getCause(separator: String = ", ", entryString: (k: String, v: Boolean) -> String = {k,v->"'$k' - ${v.toString()}"}): String {
      return this.evaluate().entries.stream().map { entryString(it.key, it.value) }.asSequence().joinToString(separator)
    }

    /**
     * named()
     * set the name of this boolean, overwrites an existing name without warning
     */
    fun named(newName: String): Bool {
        if (newName.isNotBlank()) {
            if (this is EvaluatedBool) {
                this.entries.clear()
                this.entries.add(EvaluatedBool.Entry(newName, this.value))
            }
        }
        this.name = newName
        return this
    }

    internal abstract fun evaluate(): EvaluatedBool

    /**
     * evaluated() - provides an immutable evaluated Bool
     */
    fun evaluated(): Bool = evaluate()

    /**
     * and()
     * returns a Bool representing the value of (left and right)
     * if evaluation is possible, an evaluated Bool will be returned
     */
    infix fun and(other: Bool): Bool {
        if (this.type == Type.BOOLEAN) {
            if (this.isFalse())
                return this
            if (other.type == Type.BOOLEAN) {
                if (other.isFalse())
                    return other
            }
        }
        return if (this is EvaluatedBool && other is EvaluatedBool) {
            val combinedEntries = mutableSetOf<EvaluatedBool.Entry>()
            combinedEntries.addAll(this.entries)
            combinedEntries.addAll(other.entries)
            EvaluatedBool(true, combinedEntries)
        } else {
            BinaryBool(Type.AND, this, other)
        }
    }

    /**
     * or()
     * returns a Bool representing the value of (left or right)
     * if evaluation is possible, an evaluated Bool will be returned
     */
    infix fun or(other: Bool): Bool {
        if (this.type == Type.BOOLEAN) {
            if (this.isTrue())
                return this
            if (other.type == Type.BOOLEAN) {
                if (other.isTrue())
                    return other
            }
        }
        return if (this is EvaluatedBool && other is EvaluatedBool) {
            val combinedEntries = mutableSetOf<EvaluatedBool.Entry>()
            combinedEntries.addAll(this.entries)
            combinedEntries.addAll(other.entries)
            EvaluatedBool(false, combinedEntries)
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
    operator fun not(): Bool = if (this is EvaluatedBool) {
        EvaluatedBool(this.isFalse(), this.entries)
    } else {
        NotBool(this)
    }

    /**
     * toString()
     * produces an unique value for Bool instances, that are not evaluated.
     * As soon as a Bool ist evaluated, only value and cause are relevant.
     */
    override fun toString() = "$type(name='$name')"

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

fun Boolean.asBool(name: String = ""): Bool = Bool.EvaluatedBool(this).named(name)
