package org.weasis.dicom.hernia;

/**
 * Clinical calculators for hernia CT evaluation.
 *
 * Implements:
 * 1. Carbonell's Rectus Defect Ratio (RDR)
 * 2. Tanaka Loss of Domain Ratio
 * 3. Component Separation Likelihood Prediction
 * 4. Hernia defect area classification (EHS classification)
 */
public class HerniaCalculators {

    /**
     * Calculate Carbonell's Rectus Defect Ratio (RDR).
     *
     * RDR = (Right Rectus Width + Left Rectus Width) / Hernia Defect Width
     *
     * Interpretation:
     * - RDR >= 2.0: Primary repair likely possible
     * - RDR 1.0-2.0: May need component separation or mesh bridging
     * - RDR < 1.0: Likely needs component separation or complex reconstruction
     *
     * Reference: Carbonell AM, et al. "Posterior Components Separation During
     * Retromuscular Hernia Repair" Hernia 2018.
     *
     * @param rightRectusWidthMm Right rectus abdominis width in mm
     * @param leftRectusWidthMm Left rectus abdominis width in mm
     * @param defectWidthMm Hernia defect width in mm
     * @return The RDR ratio, or NaN if defect width is 0
     */
    public double carbonellRDR(double rightRectusWidthMm,
                               double leftRectusWidthMm,
                               double defectWidthMm) {
        if (defectWidthMm <= 0 || Double.isNaN(defectWidthMm)) {
            return Double.NaN;
        }
        if (Double.isNaN(rightRectusWidthMm) || Double.isNaN(leftRectusWidthMm)) {
            return Double.NaN;
        }
        return (rightRectusWidthMm + leftRectusWidthMm) / defectWidthMm;
    }

    /**
     * Calculate Tanaka Loss of Domain ratio.
     *
     * LOD% = (Hernia Sac Volume / (Abdominal Cavity Volume + Hernia Sac Volume)) x 100
     *
     * Interpretation:
     * - LOD < 20%: Minimal loss of domain; primary repair feasible
     * - LOD 20-50%: Significant loss of domain; progressive pneumoperitoneum
     *   or component separation may be needed
     * - LOD > 50%: Severe loss of domain; high risk of abdominal compartment
     *   syndrome with primary reduction; staged approach recommended
     *
     * Reference: Tanaka EY, et al. "A computerized tomography scan method for
     * calculating the hernia sac and abdominal cavity volume in complex incisional
     * hernias with loss of domain" Hernia 2010.
     *
     * @param abdominalCavityVolumeMl Intra-abdominal cavity volume in mL
     * @param herniaSacVolumeMl Hernia sac volume in mL
     * @return Loss of domain percentage
     */
    public double tanakaLossOfDomain(double abdominalCavityVolumeMl,
                                     double herniaSacVolumeMl) {
        double totalVolume = abdominalCavityVolumeMl + herniaSacVolumeMl;
        if (totalVolume <= 0) {
            return Double.NaN;
        }
        return (herniaSacVolumeMl / totalVolume) * 100.0;
    }

    /**
     * Predict likelihood of needing component separation.
     *
     * Based on:
     * - Defect width > 10 cm
     * - RDR < 2.0
     * - LOD > 20%
     * - Combination of factors
     *
     * @param defectWidthCm Hernia defect width in cm
     * @param rdr Carbonell's RDR
     * @param lodPercent Tanaka LOD percentage
     * @return "Likely", "Possible", or "Unlikely"
     */
    public String componentSeparationLikelihood(double defectWidthCm,
                                                double rdr,
                                                double lodPercent) {
        int riskFactors = 0;

        if (defectWidthCm > 10.0) {
            riskFactors += 2;
        } else if (defectWidthCm > 6.0) {
            riskFactors += 1;
        }

        if (!Double.isNaN(rdr) && rdr < 1.5) {
            riskFactors += 2;
        } else if (!Double.isNaN(rdr) && rdr < 2.0) {
            riskFactors += 1;
        }

        if (!Double.isNaN(lodPercent) && lodPercent > 30.0) {
            riskFactors += 2;
        } else if (!Double.isNaN(lodPercent) && lodPercent > 20.0) {
            riskFactors += 1;
        }

        if (riskFactors >= 4) {
            return "Likely";
        } else if (riskFactors >= 2) {
            return "Possible";
        } else {
            return "Unlikely";
        }
    }

    /**
     * Classify hernia defect according to the European Hernia Society (EHS)
     * classification.
     *
     * Width categories: W1 (<4cm), W2 (4-10cm), W3 (>10cm)
     *
     * @param defectWidthCm Width of the hernia defect in cm
     * @return EHS width classification string
     */
    public String ehsWidthClassification(double defectWidthCm) {
        if (defectWidthCm < 4.0) {
            return "W1 (Small, <4cm)";
        } else if (defectWidthCm <= 10.0) {
            return "W2 (Medium, 4-10cm)";
        } else {
            return "W3 (Large, >10cm)";
        }
    }

    /**
     * Calculate the Sabbagh classification score for incisional hernia complexity.
     *
     * Factors:
     * - Defect width
     * - Loss of domain
     * - Recurrence (boolean)
     * - Contamination risk (boolean)
     *
     * @param defectWidthCm Defect width in cm
     * @param lodPercent Loss of domain percentage
     * @param isRecurrent Whether this is a recurrent hernia
     * @param isContaminated Whether there is contamination risk
     * @return Complexity grade: "Grade I" (simple) to "Grade IV" (complex)
     */
    public String sabbaghComplexityGrade(double defectWidthCm,
                                         double lodPercent,
                                         boolean isRecurrent,
                                         boolean isContaminated) {
        int score = 0;

        // Defect size scoring
        if (defectWidthCm > 10.0) {
            score += 3;
        } else if (defectWidthCm > 6.0) {
            score += 2;
        } else if (defectWidthCm > 4.0) {
            score += 1;
        }

        // Loss of domain
        if (!Double.isNaN(lodPercent) && lodPercent > 30.0) {
            score += 3;
        } else if (!Double.isNaN(lodPercent) && lodPercent > 20.0) {
            score += 2;
        } else if (!Double.isNaN(lodPercent) && lodPercent > 10.0) {
            score += 1;
        }

        // Recurrence
        if (isRecurrent) {
            score += 2;
        }

        // Contamination
        if (isContaminated) {
            score += 2;
        }

        if (score <= 2) {
            return "Grade I (Simple)";
        } else if (score <= 5) {
            return "Grade II (Moderate)";
        } else if (score <= 8) {
            return "Grade III (Complex)";
        } else {
            return "Grade IV (Very Complex)";
        }
    }

    /**
     * Estimate peritoneal volume deficit.
     *
     * When hernia contents have been outside the abdominal cavity long-term,
     * the peritoneal cavity shrinks. This estimates the volume deficit
     * that would need to be accommodated upon reduction.
     *
     * @param intraAbdominalVolumeMl Current intra-abdominal volume
     * @param herniaSacVolumeMl Volume in the hernia sac
     * @return Estimated volume deficit in mL that reducing the hernia would create
     */
    public double peritonealVolumeDeficit(double intraAbdominalVolumeMl,
                                          double herniaSacVolumeMl) {
        // The deficit is the hernia sac volume that needs to fit back
        // into the potentially contracted abdominal cavity
        return herniaSacVolumeMl;
    }

    /**
     * Calculate the rectus diastasis width from bilateral rectus measurements.
     *
     * The distance between the medial edges of the rectus muscles indicates
     * the width of the linea alba (diastasis if widened).
     *
     * Normal linea alba width:
     * - Above umbilicus: < 15mm (age < 45), < 27mm (age >= 45)
     * - At umbilicus: < 22mm (age < 45), < 28mm (age >= 45)
     * - Below umbilicus: < 16mm (age < 45), < 16mm (age >= 45)
     *
     * @param interRectusDistanceMm Distance between medial rectus edges in mm
     * @return Description of diastasis status
     */
    public String diastasisAssessment(double interRectusDistanceMm) {
        if (Double.isNaN(interRectusDistanceMm)) {
            return "N/A";
        }
        if (interRectusDistanceMm < 15) {
            return "Normal";
        } else if (interRectusDistanceMm < 25) {
            return "Mild Diastasis";
        } else if (interRectusDistanceMm < 40) {
            return "Moderate Diastasis";
        } else {
            return "Severe Diastasis";
        }
    }
}
