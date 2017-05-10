package test;


import com.fasterxml.jackson.databind.ObjectMapper;
import difflib.Differentiable;
import difflib.DifferentiableEntity;
import difflib.DifferentiableLevel;
import difflib.DifferentialEntityAnalyzer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Author: Yordanos Desta, on 5/2/17.
 */

@RunWith(JUnit4.class)
public class DifferentialEntityTest {


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

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);

        a3 = new A(2, 3d, "string", calendar.getTime(), a1.getUuid());

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

    /**
     * Tests with DifferentiableLevel.DEEP flag
     */

    @Test
    public void checkIfNoDifferencialDetectedDeep() {

        DifferentialEntityAnalyzer<A> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a2, DifferentiableLevel.DEEP);

        differentialEntityAnalyzer.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer.getPrettyJson().equals("{}"));

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDifferencialDetectedDeep() {

        DifferentialEntityAnalyzer differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a3, DifferentiableLevel.DEEP);

        HashMap result = differentialEntityAnalyzer.runDifferential();

        String json = differentialEntityAnalyzer.getPrettyJson();

        String expectedJson = "{\n" +
                "    \"dateValue\": \"" +
                "" + a3.getDateValue() + "\",\n" +
                "    \"doubleValue\": \"" + a3.getDoubleValue() + "\",\n" +
                "    \"intValue\": \"" + a3.getIntValue() + "\"\n" +
                "}";

        Assert.assertTrue(isJsonValid(json));

        Assert.assertTrue(areJsonEqual(json, expectedJson));

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfNoDiffDetectedWithCrossClassRefDeep() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b2, DifferentiableLevel.DEEP);
        differentialEntityAnalyzer.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer.getPrettyJson().equals("{}"));

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDiffDetectedWithCrossClassRefDeep() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b3, DifferentiableLevel.DEEP);
        differentialEntityAnalyzer.runDifferential();

        String json = differentialEntityAnalyzer.getPrettyJson();

        Assert.assertTrue(isJsonValid(json));

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDiffDetectedWith2LevelClassDepthDeep() {

        DifferentialEntityAnalyzer<C> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<>(c1, c3, DifferentiableLevel.DEEP);
        differentialEntityAnalyzer.runDifferential();

        HashMap result = differentialEntityAnalyzer.runDifferential();

        String json = differentialEntityAnalyzer.getPrettyJson();

        Assert.assertTrue(isJsonValid(json));

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfNoDiffDetectedWith2LevelClassDepthDeep() {

        DifferentialEntityAnalyzer<C> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<>(c1, c2, DifferentiableLevel.DEEP);
        differentialEntityAnalyzer.runDifferential();

        String json = differentialEntityAnalyzer.getPrettyJson();

        Assert.assertTrue(isJsonValid(json));
        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    /**
     * Tests with DifferentiableLevel.SHALLOW_UPDATE flag
     */
    @Test
    public void checkIfNoDifferentialDetectedShallowUpdate() {

        DifferentialEntityAnalyzer<A> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a2, DifferentiableLevel.SHALLOW_UPDATE);

        differentialEntityAnalyzer.runDifferential();

        String json = differentialEntityAnalyzer.getPrettyJson();

        Assert.assertTrue(isJsonValid(json));

        Assert.assertFalse(differentialEntityAnalyzer.getPrettyJson().equals("{}"));

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDifferentialDetectedShallowUpdate() {

        DifferentialEntityAnalyzer differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a3, DifferentiableLevel.SHALLOW_UPDATE);
        differentialEntityAnalyzer.runDifferential();

        String json = differentialEntityAnalyzer.getPrettyJson();

        Assert.assertTrue(isJsonValid(json));

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDiffDetectedWithCrossClassRefShallowUpdate() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b2, DifferentiableLevel.SHALLOW_UPDATE);
        differentialEntityAnalyzer.runDifferential();

        String json = differentialEntityAnalyzer.getPrettyJson();

        Assert.assertTrue(isJsonValid(json));

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

    /**
     * Tests with DifferentiableLevel.SHALLOW_IGNORE flag
     */
    @Test
    public void checkIfNoDifferentialDetectedShallowIgnore() {

        DifferentialEntityAnalyzer<A> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a2, DifferentiableLevel.SHALLOW_IGNORE);

        differentialEntityAnalyzer.runDifferential();

        String json = differentialEntityAnalyzer.getPrettyJson();

        Assert.assertTrue(isJsonValid(json));

        Assert.assertFalse(differentialEntityAnalyzer.getPrettyJson().equals("{}"));

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkIfDifferentialDetectedShallowIgnore() {

        DifferentialEntityAnalyzer differentialEntityAnalyzer = new DifferentialEntityAnalyzer<A>(a1, a3, DifferentiableLevel.SHALLOW_IGNORE);
        differentialEntityAnalyzer.runDifferential();

        String json = differentialEntityAnalyzer.getPrettyJson();

        Assert.assertTrue(isJsonValid(json));

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());
    }

    @Test
    public void checkNoIfDiffDetectedWithCrossClassRefShallowIgnore() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b2, DifferentiableLevel.SHALLOW_IGNORE);

        differentialEntityAnalyzer.runDifferential();

        String json = differentialEntityAnalyzer.getPrettyJson();

        Assert.assertTrue(isJsonValid(json));

        Assert.assertFalse(differentialEntityAnalyzer.getPrettyJson().equals("{}"));

        Assert.assertFalse(differentialEntityAnalyzer.hasDifference());


        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer2 = new DifferentialEntityAnalyzer<B>(b1, b3, DifferentiableLevel.SHALLOW_IGNORE);
        differentialEntityAnalyzer2.runDifferential();

        Assert.assertFalse(differentialEntityAnalyzer2.hasDifference());

    }

    @Test
    public void checkIfDiffDetectedWithCrossClassRefShallowIgnore() {

        DifferentialEntityAnalyzer<B> differentialEntityAnalyzer = new DifferentialEntityAnalyzer<B>(b1, b4, DifferentiableLevel.SHALLOW_IGNORE);
        differentialEntityAnalyzer.runDifferential();

        String json = differentialEntityAnalyzer.getPrettyJson();

        Assert.assertTrue(isJsonValid(json));

        Assert.assertTrue(differentialEntityAnalyzer.hasDifference());

    }

    private boolean isJsonValid(final String json) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.readTree(json);

        } catch (IOException e) {

            return false;
        }

        return true;
    }

    private boolean areJsonEqual(String json1, String json2) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            Map<String, String> j1 = objectMapper.readValue(json1, Map.class),

                    j2 = objectMapper.readValue(json2, Map.class);

            if (j1.size() != j2.size())
                return false;

            for (String k : j1.keySet()) {

                if (j1.get(k) != null && j2.get(k) == null)
                    return false;

                if (!j1.get(k).equals(j2.get(k)))
                    return false;
            }

            return true;

        } catch (IOException e) {

            e.printStackTrace();
        }

        return false;
    }

}
