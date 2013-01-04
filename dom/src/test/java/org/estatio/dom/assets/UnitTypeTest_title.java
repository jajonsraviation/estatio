package org.estatio.dom.assets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.estatio.dom.asset.UnitType;
import org.junit.Test;


public class UnitTypeTest_title {

    @Test
    public void test() {
        assertThat(UnitType.BOUTIQUE.title(), is("Boutique"));
        assertThat(UnitType.HYPERMARKET.title(), is("Hypermarket"));
    }


}
