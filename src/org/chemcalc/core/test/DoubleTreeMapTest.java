package org.chemcalc.core.test;


import org.chemcalc.core.DoubleTreeMap;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DoubleTreeMapTest {

    @Test
    public void combine()  {

        DoubleTreeMap dtm = new DoubleTreeMap();
        dtm.put(new Double(100), new Double(10));
        dtm.put(new Double(101), new Double(20));
        dtm.put(new Double(101.1), new Double(30));
        dtm.put(new Double(101.2), new Double(40));
        dtm.put(new Double(102), new Double(50));

        dtm.combine(0.50);

        assertArrayEquals(dtm.toXYArray()[0],new double[]{100,10},0.00001);
        assertArrayEquals(dtm.toXYArray()[1],new double[]{101,90},0.00001);
        assertArrayEquals(dtm.toXYArray()[2],new double[]{102,50},0.00001);
    }

    @Test
    public void combineProportional()  {

        DoubleTreeMap dtm = new DoubleTreeMap();
        dtm.put(new Double(100), new Double(10));
        dtm.put(new Double(101), new Double(20));
        dtm.put(new Double(101.1), new Double(30));
        dtm.put(new Double(101.2), new Double(40));
        dtm.put(new Double(102), new Double(50));

        dtm.combineProportional(0.50);

        assertArrayEquals(dtm.toXYArray()[0],new double[]{100,10},0.00001);
        assertArrayEquals(dtm.toXYArray()[1],new double[]{101.1222222,90},0.00001);
        assertArrayEquals(dtm.toXYArray()[2],new double[]{102,50},0.00001);
    }



}
