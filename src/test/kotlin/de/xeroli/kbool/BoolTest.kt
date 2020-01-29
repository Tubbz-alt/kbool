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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoolTest {
    @Test
    fun testSupplier() {
        var a: String? = null
        var notNull = Bool.of { a != null }.named("String is not null")
        var longEnough = Bool.of { (a!!.length > 7) }.named("String has at least 7 characters")

        assertFalse((notNull and longEnough).isTrue(), "false because of a is null")
        assertEquals("String is not null - false", (notNull and longEnough).getCause(), "should be 'String is not null - false'")

        a = "Hallo Welt!"
        assertTrue((notNull and longEnough).isTrue(), "false because of a is null")
        assertEquals("String is not null - true, String has at least 7 characters - true", (notNull and longEnough).getCause(), "should be 'String is not null - false'")

        var i = 1
        val bool = Bool.of { i > 10 }.named("i greater than 10?")
        assertFalse(bool.booleanValue())
        i = 100
        assertTrue(bool.booleanValue())
    }

    @Test
    fun testSomeSimpleBools() {
        val sunIsShining = true.asBool()
        assertTrue(sunIsShining.isTrue(), "sunIsShining should return 'true'")

        val isRaining = Bool.SupplierBool({ false.asBool() }).named("isRaining")
        assertFalse((!isRaining).isFalse(), "isRaining should return 'false'")

        val haveUmbrella = true.asBool("haveUmbrella")

        val walkingInTheWood = sunIsShining.named("sunIsShining") and (!isRaining or haveUmbrella)
        assertTrue(walkingInTheWood.isTrue(), "walkingInTheWood should return 'true'")

        assertEquals("[sunIsShining - true, isRaining - false]",
                walkingInTheWood.getCause(prefix = "[", postfix = "]"),
                "getCause shouldd return '[sunIsShining - true, isRaining - false]'")

        walkingInTheWood.named("walk")
        assertEquals("[walk - true]",
                walkingInTheWood.getCause(prefix = "[", postfix = "]"),
                "getCause shouldd return '[walk - true]'")

    }

    @Test
    fun testNaming() {
        val simpleBool = true.asBool()
        assertEquals("TRUE - true", simpleBool.getCause(), "unnamed Bool has simple cause")

        val directlyNamed = true.asBool("directly")
        assertEquals("directly - true", directlyNamed.getCause(), "directly named Bool has wrong cause")

        val lateNamed = true.asBool().named("late")
        assertEquals("late - true", lateNamed.getCause(), "lately named Bool has wrong cause")

        val xorBool = (directlyNamed and !lateNamed) or (!directlyNamed and lateNamed)
        assertEquals("late - true, directly - true", xorBool.getCause(), "xorBool named Bool has wrong cause")
    }

    @Test
    fun testEquality() {
        var directlyNamed = true.asBool("directly")
        var lateNamed = true.asBool().named("directly")
        assertTrue(directlyNamed == lateNamed, "false ")

        directlyNamed.booleanValue()
        lateNamed.booleanValue()
        assertTrue(directlyNamed == lateNamed, "naming time has no impact on equality")

        directlyNamed = true.asBool("directly")
        lateNamed = true.asBool().named("directly")

        // implicitly evaluate one of them
        lateNamed.booleanValue()
        assertTrue(directlyNamed == lateNamed, "naming time has no impact on equality")

        var aNot = !directlyNamed
        var otherNot = !directlyNamed
        assertTrue(aNot == otherNot, "two not's of same bool should be equal")

        aNot = !directlyNamed
        otherNot = directlyNamed.not().named("nonono")
        assertFalse(aNot == otherNot, "two not's of same bool but renamed should not be equal")
    }

    @Test
    fun testToString() {
        val TRUE = true.asBool()
        assertEquals("BOOLEAN(name='', value=true, entries=[Entry('TRUE': true)])", TRUE.toString(), "TRUE.toString()")

        val FALSE = false.asBool()
        assertEquals("BOOLEAN(name='', value=false, entries=[Entry('FALSE': false)])", FALSE.toString(), "FALSE.toString()")

        FALSE.named("namedFalse")
        assertEquals("BOOLEAN(name='namedFalse', value=false, entries=[Entry('namedFalse': false)])", FALSE.toString(), "namedFalse.toString()")

        val otherTrue = Bool.of(true)
        assertEquals("BOOLEAN(name='', value=true, entries=[Entry('TRUE': true)])", otherTrue.toString(), "otherTrue.toString()")

        var a = 7
        val directlyEvaluatedTrue = Bool.of(a < 10)
        assertEquals("BOOLEAN(name='', value=true, entries=[Entry('TRUE': true)])", directlyEvaluatedTrue.toString(), "directlyEvaluatedTrue.toString()")

        val deferredEvaluatedTrue = Bool.of { a < 10 }
        assertTrue(deferredEvaluatedTrue.toString().startsWith("SUPPLIER(name='', supplierHash=#"), "deferredEvaluatedTrue.toString()")

        deferredEvaluatedTrue.named("namedDeferred")
        assertTrue(deferredEvaluatedTrue.toString().startsWith("SUPPLIER(name='namedDeferred', supplierHash=#"), "namedDeferred.toString()")

        deferredEvaluatedTrue.booleanValue()
        assertTrue(deferredEvaluatedTrue.toString().startsWith("SUPPLIER(name='namedDeferred', supplierHash=#"), "deferredEvaluatedTrue.toString() after booleanValue()")

        val evaluated = deferredEvaluatedTrue.evaluated()
        assertEquals("BOOLEAN(name='namedDeferred', value=true, entries=[Entry('namedDeferred': true)])", evaluated.toString(), "evaluated.toString()")

        assertTrue(deferredEvaluatedTrue.booleanValue() == evaluated.booleanValue(), "deferred and evaluated should be synchronous by value")
        assertTrue(deferredEvaluatedTrue.getCause() == evaluated.getCause(), "deferred and evaluated should be synchronous by cause")

        a = 12

        assertFalse(deferredEvaluatedTrue.booleanValue() == evaluated.booleanValue(), "deferred and evaluated should not be synchronous by value")
        assertFalse(deferredEvaluatedTrue.getCause() == evaluated.getCause(), "deferred and evaluated should not be synchronous by cause")
    }
}
