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

internal abstract class BinaryBool internal constructor(private val left: Bool, private val right: Bool) : Bool() {

    abstract fun isShortCircuitPossible(innerBool: EvaluatedBool): Boolean
    abstract fun applyBinaryFunction(innerLeft: EvaluatedBool, innerRight: EvaluatedBool): Boolean

    override fun evaluate(): EvaluatedBool {
        val innerLeft = left.evaluate()
        if (isShortCircuitPossible(innerLeft))
            return innerLeft.rename(this.name)

        val innerRight = right.evaluate()
        if (isShortCircuitPossible(innerRight))
            return innerRight.rename(this.name)

        val innerValue = applyBinaryFunction(innerLeft, innerRight)
        val innerEntries = mutableSetOf<EvaluatedBool.Entry>()
        innerEntries.addAll(innerLeft.entries)
        innerEntries.addAll(innerRight.entries)
        return EvaluatedBool(innerValue, innerEntries).rename(this.name)
    }

    override fun toString() = "BINARY(name='$name', left=$left, right=$right)"
}
