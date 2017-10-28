package com.rainmachine;

import com.rainmachine.domain.util.MetricCalculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetricCalculatorTest {

    @Test
    public void celsiusToFahrenheitWorksCorrectly() {
        float val1 = MetricCalculator.celsiusToFahrenheit(0f);
        assertEquals(val1, 32f, 0.1f);
        float val2 = MetricCalculator.celsiusToFahrenheit(100f);
        assertEquals(val2, 212f, 0.1f);
    }

    @Test
    public void fahrenheitToCelsiusWorksCorrectly() {
        float val1 = MetricCalculator.fahrenheitToCelsius(32f);
        assertEquals(val1, 0f, 0.1f);
        float val2 = MetricCalculator.fahrenheitToCelsius(212f);
        assertEquals(val2, 100f, 0.1f);
    }

    @Test
    public void mmToInchWorksCorrectly() {
        float val1 = MetricCalculator.mmToInch(1);
        assertEquals(val1, 0.039f, 0.1f);
        float val2 = MetricCalculator.mmToInch(25.4f);
        assertEquals(val2, 1f, 0.1f);
    }

    @Test
    public void inchToMmWorksCorrectly() {
        float val1 = MetricCalculator.inchToMm(1);
        assertEquals(val1, 25.4f, 0.1f);
        float val2 = MetricCalculator.inchToMm(0.039f);
        assertEquals(val2, 1f, 0.1f);
    }
}
