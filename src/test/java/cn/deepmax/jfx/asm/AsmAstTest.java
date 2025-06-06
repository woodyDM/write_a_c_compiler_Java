package cn.deepmax.jfx.asm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AsmAstTest {

    @Test
    void test_stack_align() {

        assertEquals(AsmAst.get16AlignedStack(1),16);
        assertEquals(AsmAst.get16AlignedStack(2),16);
        assertEquals(AsmAst.get16AlignedStack(3),16);
        assertEquals(AsmAst.get16AlignedStack(4),16);
        assertEquals(AsmAst.get16AlignedStack(5),32);
        assertEquals(AsmAst.get16AlignedStack(6),32);
        assertEquals(AsmAst.get16AlignedStack(7),32);
        assertEquals(AsmAst.get16AlignedStack(8),32);
        assertEquals(AsmAst.get16AlignedStack(9),48);
    }
}