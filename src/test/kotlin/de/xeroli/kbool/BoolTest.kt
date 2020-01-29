package de.xeroli.kbool

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoolTest {
    @Test
    fun testSupplier() {
        var a: String? = null
        var notNull = Bool.of{a != null}.named("String is not null")
        var longEnough = Bool.of{ (a!!.length > 7) }.named("String has at least 7 characters")

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
        assertEquals("", simpleBool.getCause(), "unnamed Bool has no cause")

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
        assertFalse(directlyNamed == lateNamed, "false ")

        directlyNamed.booleanValue()
        lateNamed.booleanValue()
        assertTrue(directlyNamed == lateNamed, "naming time has no impact on equality")

        directlyNamed = true.asBool("directly")
        lateNamed = true.asBool().named("directly")

        // implicitly evaluate one of them
        lateNamed.booleanValue()
        assertFalse(directlyNamed == lateNamed, "naming time has no impact on equality")

        var aNot = !directlyNamed
        var otherNot = !directlyNamed
        assertTrue(aNot == otherNot, "two not's of same bool should be equal")

        aNot = !directlyNamed
        otherNot = directlyNamed.not().named("nonono")
        assertFalse(aNot == otherNot, "two not's of same bool but renamed should not be equal")
    }

}
