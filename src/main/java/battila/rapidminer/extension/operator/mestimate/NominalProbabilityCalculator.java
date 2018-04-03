package battila.rapidminer.extension.operator.mestimate;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.tools.LogService;

final class NominalProbabilityCalculator implements ProbabilityCalculator {
    private final double m;

    private final Map<Double, Double> priors;

    private final Map<Double, Map<Double, Integer>> valueCountPerClass;

    private final Map<Double, Integer> countPerClass;

    private NominalProbabilityCalculator(Builder builder) {
        this.m = builder.m;
        this.priors = builder.priors;
        this.valueCountPerClass =  builder.valueCountPerClass;
        this.countPerClass = builder.countPerClass;
    }

    @Override
    public double calculateFor(double attributeValue, double clazz) {
        final int count = valueCountPerClass.get(attributeValue).get(clazz);

        final double numerator = (double)count + m * priors.get(clazz);

        return numerator / (countPerClass.get(clazz).doubleValue() + m);
    }

    static final class Builder implements ProbabilityCalculator.Builder {
        private final Attribute targetAttribute;

        private final double m;

        private final Map<Double, Double> priors;

        private final Map<Double, Map<Double, Integer>> valueCountPerClass;

        private final Map<Double, Integer> countPerClass;

        private Attribute labelAttribute;

        Builder(Attribute targetAttribute, double m, Map<Double, Double> priors) {
            this.targetAttribute = targetAttribute;
            this.m = m;
            this.priors = priors;
            this.valueCountPerClass = new HashMap<>();
            this.countPerClass = new HashMap<>();
        }

        @Override
        public void add(Example example) {
            final Map<Double, Integer> classCounts =
                    valueCountPerClass.computeIfAbsent(example.getValue(targetAttribute), value -> new HashMap<>());

            classCounts.merge(example.getLabel(), 1, Integer::sum);

            countPerClass.merge(example.getLabel(), 1, Integer::sum);

            labelAttribute = example.getAttributes().getLabel();
        }

        @Override
        public ProbabilityCalculator build() {
            LogService.getRoot().info(targetAttribute.getName());

            for (Map.Entry<Double, Map<Double, Integer>> entry : valueCountPerClass.entrySet()) {
                LogService.getRoot().info(targetAttribute.getMapping().mapIndex(entry.getKey().intValue()));

                for (Map.Entry<Double, Integer> e : entry.getValue().entrySet()) {
                    LogService.getRoot().info("  " + labelAttribute.getMapping().mapIndex(e.getKey().intValue()) + " " + e.getValue().toString());
                }
            }

            return new NominalProbabilityCalculator(this);
        }
    }
}
