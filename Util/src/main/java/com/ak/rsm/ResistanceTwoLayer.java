package com.ak.rsm;

import java.util.function.DoubleUnaryOperator;

import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.TrivariateFunction;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>2-layer</b> model.
 */
final class ResistanceTwoLayer implements TrivariateFunction, Cloneable {
  private final ResistanceOneLayer resistanceOneLayer;

  ResistanceTwoLayer(TetrapolarSystem electrodeSystem) {
    resistanceOneLayer = new ResistanceOneLayer(electrodeSystem);
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rho1SI specific resistance of <b>1st-layer</b> in Ohm-m
   * @param rho2SI specific resistance of <b>2nd-layer</b> in Ohm-m
   * @param hSI    height of <b>1-layer</b> in metres
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  @Override
  public double value(double rho1SI, double rho2SI, double hSI) {
    double resistivity = resistanceOneLayer.value(rho1SI);

    if (Double.compare(rho1SI, rho2SI) == 0) {
      return resistivity;
    }
    else {
      return resistivity + 2.0 * ResistanceOneLayer.thoRhoByPI(rho1SI) * sum(getK12(rho1SI, rho2SI), hSI);
    }
  }

  static double getRho1ToRho2(double k12) {
    return (1.0 - k12) / (1.0 + k12);
  }

  static double getK12(double rho1SI, double rho2SI) {
    return (rho2SI - rho1SI) / (rho2SI + rho1SI);
  }

  static double sum(double hSI, BivariateFunction nAndB) {
    return sum(n -> nAndB.value(n, 4.0 * n * hSI));
  }

  double sum(double k12, double hSI) {
    return sum(hSI, (n, b) -> pow(k12, n) *
        (1.0 / hypot(resistanceOneLayer.getElectrodeSystem().radiusMinus(), b)
            - 1.0 / hypot(resistanceOneLayer.getElectrodeSystem().radiusPlus(), b)));
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  private static double sum(DoubleUnaryOperator operator) {
    double sum = 0.0;
    for (int n = 1; ; n++) {
      double prev = sum;
      sum += operator.applyAsDouble(n);
      if (Double.compare(prev, sum) == 0) {
        break;
      }
    }
    return sum;
  }
}
