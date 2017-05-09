package test;


import difflib.Differentiable;
import difflib.DifferentiableEntity;
import difflib.DifferentialEntityAnalyzer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

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

    @Test
    public void checkIfNoDifferencialDetected() {

        DifferentialEntityAnalyzer<A> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a2);

        differentialEntityAnalyzer.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDifferencialDetected() {

        DifferentialEntityAnalyzer differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a3);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfNoDiffDetectedWithCrossClassRef() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b2);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDiffDetectedWithCrossClassRef() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b3);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDiffDetectedWith2LevelClassDepth(){

        DifferentialEntityAnalyzer<C> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<>(c1, c3);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfNoDiffDetectedWith2LevelClassDepth(){

        DifferentialEntityAnalyzer<C> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<>(c1, c2);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }
}
