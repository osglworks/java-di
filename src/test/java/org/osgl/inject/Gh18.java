package org.osgl.inject;

import org.junit.Test;
import osgl.ut.TestBase;

public class Gh18 extends TestBase {

    @Test
    public void test() {
        Genie genie = Genie.create();
        BeanSpec spec = BeanSpec.of(int[].class, genie);
        eq(int.class, spec.typeParams().get(0));
    }

}
