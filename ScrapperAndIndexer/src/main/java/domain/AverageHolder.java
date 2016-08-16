package domain;

import lombok.Data;

/**
 * For testing and benchmarking purposes only
 */
@Data
public class AverageHolder {
    private double avg;
    private int cnt;

    @Override
    public String toString() {
        return String.format("%.2f", avg);
    }
}
