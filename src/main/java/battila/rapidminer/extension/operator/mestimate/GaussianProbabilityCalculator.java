package battila.rapidminer.extension.operator.mestimate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;

final class GaussianProbabilityCalculator implements ProbabilityCalculator {
    private final Map<Double, DistributionProperties> distributionPropertyMap;

    private GaussianProbabilityCalculator(Builder builder) {
        this.distributionPropertyMap = builder.distributionPropertyMap;
    }

    @Override
    public double calculateFor(double attributeValue, double clazz) {
        final DistributionProperties props = distributionPropertyMap.get(clazz);

        final double pow = -(Math.pow(attributeValue - props.mean, 2) / (2 * props.variance));

        final double rat = 1.0 / Math.sqrt(2 * Math.PI * props.variance);

        return rat * Math.exp(pow);
    }

    private static final class DistributionProperties {
        private final double mean;

        private final double variance;

        private DistributionProperties(double mean, double variance) {
            this.mean = mean;
            this.variance = variance;
        }

        private static DistributionProperties fromValues(List<Double> values) {
            final double n = (double)values.size();

            final double mean = values.stream().reduce(0.0, Double::sum) / n;

            final double variance = (1.0 / n) * values.stream()
                    .map(x -> Math.pow(x - mean, 2))
                    .reduce(0.0, Double::sum);

            return new DistributionProperties(mean, variance);
        }
    }

    static final class Builder implements ProbabilityCalculator.Builder {
        private final Attribute targetAttribute;

        private final Map<Double, List<Double>> valuesPerClass;

        private final Map<Double, DistributionProperties> distributionPropertyMap;

        Builder(Attribute targetAttribute) {
            this.targetAttribute = targetAttribute;
            this.valuesPerClass = new HashMap<>();
            this.distributionPropertyMap = new HashMap<>();
        }

        @Override
        public void add(Example example) {
            final List<Double> list  = valuesPerClass.computeIfAbsent(example.getLabel(), value -> new ArrayList<>());

            list.add(example.getValue(targetAttribute));
        }

        @Override
        public ProbabilityCalculator build() {
            for (Map.Entry<Double, List<Double>> entry : valuesPerClass.entrySet()) {
                distributionPropertyMap.put(entry.getKey(), DistributionProperties.fromValues(entry.getValue()));
            }

            return new GaussianProbabilityCalculator(this);
        }
    }
}
