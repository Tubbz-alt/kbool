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

import java.util.*

internal class EvaluatedBool(internal var value: Boolean, internal val entries: MutableSet<Entry> = mutableSetOf(Entry("NONAME_${UUID.randomUUID().toString()}", value))) : Bool() {

    internal data class Entry(val key: String, val value: Boolean) {
        override fun toString() = "Entry('$key': $value)"
    }

    override fun evaluate() = this
    override fun toString() = "BOOLEAN(name='$name', value=$value, entries=$entries)"

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
