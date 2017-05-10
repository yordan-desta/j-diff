package test;


import difflib.Differentiable;
import difflib.DifferentiableEntity;
import difflib.DifferentiableLevel;
import difflib.DifferentialEntityAnalyzer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Author: Yordanos Desta, on 5/2/17.
 */

@RunWith(JUnit4.class)
public class DifferentialEntityTest{


    @Differentiable(ignoreDiff = false)
    class A extends DifferentiableEntity {

        private Integer intValue;
        private Double doubleValue;
        private String stringValue;
        private Date dateValue;


        A(Integer intValue, Double doubleValue, String stringValue, Date dateValue, String uuid) {

            this.intValue = intValue;
            this.doubleValue = doubleValue;
            this.stringValue = stringValue;
            this.dateValue = dateValue;
            this.uuid = uuid;
        }

        String getUUid() {
            return this.uuid;
        }

        public Integer getIntValue() {
            return intValue;
        }

        public Double getDoubleValue() {
            return doubleValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        public Date getDateValue() {
            return dateValue;
        }
    }

    class B extends DifferentiableEntity {

        private Integer intValue;
        private Double doubleValue;

        private A a;


        B(Integer intValue, Double doubleValue, String uuid, A a) {

            this.intValue = intValue;
            this.doubleValue = doubleValue;

            this.a = a;

            this.uuid = uuid;
        }

        public Integer getIntValue() {
            return intValue;
        }

        public Double getDoubleValue() {
            return doubleValue;
        }

        public A getA() {
            return a;
        }
    }

    class C extends DifferentiableEntity {

        private B b;


        public B getB() {
            return b;
        }

        public void setB(B b) {
            this.b = b;
        }
    }

    private A a1, a2, a3;
    private B b1, b2, b3, b4;
    private C c1, c2, c3;

    @Before
    public void setUp() {

        a1 = new A(1, 2d, "string", Calendar.getInstance().getTime(), UUID.randomUUID().toString());
        a2 = new A(1, 2d, "string", a1.getDateValue(), a1.getUuid());

        a3 = new A(2, 3d, "string", Calendar.getInstance().getTime(), a1.getUuid());

        final String b_uuid = UUID.randomUUID().toString();

        b1 = new B(1, 2d, b_uuid, a1);
        b2 = new B(1, 2d, b_uuid, a2);

        b3 = new B(1, 2d, b_uuid, a3);
        b4 = new B(1, 3d, b_uuid, a1);

        c1 = new C();
        c1.setB(b1);

        c2 = new C();
        c2.setB(b2);

        c3 = new C();
        c3.setB(b3);

    }

    ///DEEP

    @Test
    public void checkIfNoDifferencialDetectedDeep() {

        DifferentialEntityAnalyzer<A> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a2, DifferentiableLevel.DEEP);

        differentialEntityAnalyzer.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDifferencialDetectedDeep() {

        DifferentialEntityAnalyzer differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a3, DifferentiableLevel.DEEP);

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfNoDiffDetectedWithCrossClassRefDeep() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b2, DifferentiableLevel.DEEP);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDiffDetectedWithCrossClassRefDeep() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b3, DifferentiableLevel.DEEP);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDiffDetectedWith2LevelClassDepthDeep(){

        DifferentialEntityAnalyzer<C> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<>(c1, c3, DifferentiableLevel.DEEP);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfNoDiffDetectedWith2LevelClassDepthDeep(){

        DifferentialEntityAnalyzer<C> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<>(c1, c2, DifferentiableLevel.DEEP);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    ////SHALLOW_UPDATE

    @Test
    public void checkIfNoDifferentialDetectedShallowUpdate() {

        DifferentialEntityAnalyzer<A> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a2, DifferentiableLevel.SHALLOW_UPDATE);

        differentialEntityAnalyzer.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDifferentialDetectedShallowUpdate() {

        DifferentialEntityAnalyzer differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a3, DifferentiableLevel.SHALLOW_UPDATE);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDiffDetectedWithCrossClassRefShallowUpdate() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b2, DifferentiableLevel.SHALLOW_UPDATE);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());


        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer2 = new DifferentialEntityAnalyzer<B>(b1, b3, DifferentiableLevel.SHALLOW_UPDATE);
        differentialEntityAnalyzer2.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer2.hasDifference());

        DifferentialEntityAnalyzer<C> differentialEntityAnalyzer3 = new DifferentialEntityAnalyzer<>(c1, c3, DifferentiableLevel.SHALLOW_UPDATE);
        differentialEntityAnalyzer3.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer3.hasDifference());

        DifferentialEntityAnalyzer<C> differentialEntityAnalyzer4 = new DifferentialEntityAnalyzer<>(c1, c2, DifferentiableLevel.SHALLOW_UPDATE);
        differentialEntityAnalyzer4.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer4.hasDifference());
    }

    ///SHALLOW_IGNORE

    @Test
    public void checkIfNoDifferentialDetectedShallowIgnore() {

        DifferentialEntityAnalyzer<A> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a2, DifferentiableLevel.SHALLOW_IGNORE);

        differentialEntityAnalyzer.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDifferentialDetectedShallowIgnore() {

        DifferentialEntityAnalyzer differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a3, DifferentiableLevel.SHALLOW_IGNORE);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkNoIfDiffDetectedWithCrossClassRefShallowIgnore() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b2, DifferentiableLevel.SHALLOW_IGNORE);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());


        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer2 = new DifferentialEntityAnalyzer<B>(b1, b3, DifferentiableLevel.SHALLOW_IGNORE);
        differentialEntityAnalyzer2.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer2.hasDifference());

    }

    @Test
    public void checkIfDiffDetectedWithCrossClassRefShallowIgnore() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b4, DifferentiableLevel.SHALLOW_IGNORE);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());

    }

}
