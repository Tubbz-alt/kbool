package de.xeroli.kbool

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoolTest {
    @Test
    fun testSomeSimpleBools() {
        val sunIsShining = true.asBool()
        assertTrue(sunIsShining.isTrue(), "sunIsShining should return 'true'")

        val isRaining = Bool.SupplierBool({ false.asBool() }).named("isRaining")
        assertTrue((!isRaining).isTrue(), "isRaining should return 'false'")

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
}
