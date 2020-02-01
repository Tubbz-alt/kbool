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

import kotlin.test.*

/**
 * overall tests
 */
class BoolTest {

    @Test
    fun testSomeSupplierBools() {
        var a: String? = null
        val notNull = Bool.of { a != null }.named("String is not null")
        val longEnough = Bool.of { a!!.length > 7 }.named("String has at least 7 characters")

        assertFalse((notNull and longEnough).isTrue(), "false because of 'a' is null")
        assertEquals("'String is not null' - false", (notNull and longEnough).getCause(), "should be 'String is not null - false'")

        a = "Hallo Welt!"
        assertTrue((notNull and longEnough).isTrue(), "false because of 'a' is null")
        assertEquals("'String is not null' - true, 'String has at least 7 characters' - true", (notNull and longEnough).getCause(), "should be 'String is not null - false'")

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

        val isRaining = Bool.SupplierBool { false.asBool() }.named("isRaining")
        assertFalse((!isRaining).isFalse(), "isRaining should return 'false'")

        val haveUmbrella = true.asBool("haveUmbrella")

        val walkingInTheWood = sunIsShining.named("sunIsShining") and (!isRaining or haveUmbrella)
        assertTrue(walkingInTheWood.isTrue(), "walkingInTheWood should return 'true'")

        assertEquals("'sunIsShining' - true, 'isRaining' - false",
                walkingInTheWood.getCause(),
                "getCause shouldd return 'sunIsShining - true, isRaining - false'")

        walkingInTheWood.named("walk")
        assertEquals("'walk' - true",
                walkingInTheWood.getCause(),
                "getCause should return 'walk' - true")
    }

    @Test
    fun testNaming() {
        val simpleBool = true.asBool()
        assertEquals("'TRUE' - true", simpleBool.getCause(), "unnamed Bool has simple cause")

        val directlyNamed = true.asBool("directly")
        assertEquals("'directly' - true", directlyNamed.getCause(), "directly named Bool has wrong cause")

        val lateNamed = true.asBool().named("late")
        assertEquals("'late' - true", lateNamed.getCause(), "lately named Bool has wrong cause")

        val xorBool = (directlyNamed and !lateNamed) or (!directlyNamed and lateNamed)
        assertEquals("'late' - true, 'directly' - true", xorBool.getCause(), "xorBool named Bool has wrong cause")
    }

    @Test
    fun testEquality() {
        val uniqueName = "uniqueName";

        var firstBool = true.asBool(uniqueName)
        var secondBool = true.asBool().named(uniqueName)
        assertEquals(firstBool, secondBool, "Bool with name equals Bool with name by named()")

        assertEquals(firstBool.booleanValue(), secondBool.booleanValue(), "naming time has no impact on equality")

        firstBool = true.asBool(uniqueName)

        var aNot = !firstBool
        var otherNot = !firstBool
        assertEquals(aNot, otherNot, "two not's of same bool should be equal")

        aNot = !firstBool
        otherNot = firstBool.not().named("no-no-no")
        assertNotEquals(aNot, otherNot, "two not's of same bool but renamed should not be equal")
    }

    @Test
    fun testToString() {
        val a = true.asBool()
        assertEquals("BOOLEAN(name='', value=true, entries=[Entry('TRUE': true)])", a.toString(), "a.toString()")

        val b = false.asBool()
        assertEquals("BOOLEAN(name='', value=false, entries=[Entry('FALSE': false)])", b.toString(), "b.toString()")

        b.named("namedFalse")
        assertEquals("BOOLEAN(name='namedFalse', value=false, entries=[Entry('namedFalse': false)])", b.toString(), "namedFalse.toString()")

        val otherTrue = Bool.of(true)
        assertEquals("BOOLEAN(name='', value=true, entries=[Entry('TRUE': true)])", otherTrue.toString(), "otherTrue.toString()")

        var i = 7
        val directlyEvaluatedTrue = Bool.of(i < 10)
        assertEquals("BOOLEAN(name='', value=true, entries=[Entry('TRUE': true)])", directlyEvaluatedTrue.toString(), "directlyEvaluatedTrue.toString()")

        val deferredEvaluatedTrue = Bool.of { i < 10 }
        assertTrue(deferredEvaluatedTrue.toString().startsWith("SUPPLIER(name='', supplierHash=#"), "deferredEvaluatedTrue.toString()")

        deferredEvaluatedTrue.named("namedDeferred")
        assertTrue(deferredEvaluatedTrue.toString().startsWith("SUPPLIER(name='namedDeferred', supplierHash=#"), "namedDeferred.toString()")

        deferredEvaluatedTrue.booleanValue()
        assertTrue(deferredEvaluatedTrue.toString().startsWith("SUPPLIER(name='namedDeferred', supplierHash=#"), "deferredEvaluatedTrue.toString() after booleanValue()")

        val evaluated = deferredEvaluatedTrue.evaluated()
        assertEquals("BOOLEAN(name='namedDeferred', value=true, entries=[Entry('namedDeferred': true)])", evaluated.toString(), "evaluated.toString()")

        assertEquals(deferredEvaluatedTrue.booleanValue(), evaluated.booleanValue(), "deferred and evaluated should be synchronous by value")
        assertEquals(deferredEvaluatedTrue.getCause(), evaluated.getCause(), "deferred and evaluated should be synchronous by cause")

        i = 12

        assertNotEquals(deferredEvaluatedTrue.booleanValue(), evaluated.booleanValue(), "deferred and evaluated should not be synchronous by value")
        assertNotEquals(deferredEvaluatedTrue.getCause(), evaluated.getCause(), "deferred and evaluated should not be synchronous by cause")
    }

    @Test
    fun testConstructingNotBoolToString() {
        val i = 7
        val deferredEvaluatedTrue = Bool.of { i < 10 }
        deferredEvaluatedTrue.named("namedDeferred")
        val a = true.asBool()

        assertEquals("BOOLEAN(name='', value=false, entries=[Entry('TRUE': true)])", a.not().toString(), "not on an evaluated Bool")
        assertTrue(deferredEvaluatedTrue.not().toString().startsWith("NOT(name='', SUPPLIER(name='namedDeferred', supplierHash=#"), "not on an deferred Bool")
    }

    @Test
    fun testConstructingAndBoolToString() {
        val i = 7
        val deferredEvaluatedTrue = Bool.of { i < 10 }
        deferredEvaluatedTrue.named("namedDeferred")
        val a = true.asBool().named("A")
        val b = true.asBool().named("B")
        val c = false.asBool().named("C")

        val stringOfC = c.toString()

        assertEquals(stringOfC, (c and a).toString(), "and on an evaluated Bools (left plays)")
        assertEquals(stringOfC, (a and c).toString(), "and on an evaluated Bools (right plays)")
        assertEquals("BOOLEAN(name='', value=true, entries=[Entry('A': true), Entry('B': true)])", (a and b).toString(), "and on an evaluated Bools (both play)")
        assertTrue((a and deferredEvaluatedTrue).toString().startsWith("AND(name='', left=BOOLEAN(name='A', value=true, entries=[Entry('A': true)]), right=SUPPLIER(name='namedDeferred', supplierHash=#"), "and on deferred")
        assertEquals(stringOfC, (c and deferredEvaluatedTrue).toString(), "and on decidable deferred Bool (left plays)")
    }

    @Test
    fun testConstructingOrBoolToString() {
        val i = 7
        val deferredEvaluatedTrue = Bool.of { i < 10 }
        deferredEvaluatedTrue.named("namedDeferred")
        val a = true.asBool().named("A")
        val b = false.asBool().named("B")
        val c = false.asBool().named("C")

        val stringOfA = a.toString()

        assertEquals(stringOfA, (a or c).toString(), "or on an evaluated Bools (left plays)")
        assertEquals(stringOfA, (c or a).toString(), "or on an evaluated Bools (right plays)")
        assertEquals("BOOLEAN(name='', value=false, entries=[Entry('C': false), Entry('B': false)])",
                (c or b).toString(), "or on an evaluated Bools (both play)")
        assertTrue((c or deferredEvaluatedTrue).toString().startsWith(
                "OR(name='', left=BOOLEAN(name='C', value=false, entries=[Entry('C': false)]), right=SUPPLIER(name='namedDeferred', supplierHash=#"),
                "or on deferred")
        assertEquals(stringOfA, (a or deferredEvaluatedTrue).toString(), "or on decidable deferred Bool (left plays)")
    }

    @Test
    fun testGetCause() {
        val a = true.asBool("a")
        val b = false.asBool("b")
        val c = (a and !b).evaluated()
        assertEquals("'a' - true, 'b' - false", c.getCause(), "getCause()")
        assertEquals("a and (not b)", c.getCause(" and ") { k, v -> if (v) k else "(not $k)" }, "getCause() alternative string")
    }

}
