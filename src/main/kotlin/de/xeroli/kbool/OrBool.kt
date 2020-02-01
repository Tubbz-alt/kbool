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

internal class OrBool(private val left: Bool, private val right: Bool) : BinaryBool(left, right) {
    override fun isShortCircuitPossible(innerBool: EvaluatedBool) = innerBool.value
    override fun applyBinaryFunction(innerLeft: EvaluatedBool, innerRight: EvaluatedBool) = innerLeft.value or innerRight.value
    override fun toString() = "OR(name='$name', left=$left, right=$right)"
}
