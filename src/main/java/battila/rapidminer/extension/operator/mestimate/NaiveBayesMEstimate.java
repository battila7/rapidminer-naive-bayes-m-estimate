package battila.rapidminer.extension.operator.mestimate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.LogService;

import static java.util.stream.Collectors.toMap;

public class NaiveBayesMEstimate extends AbstractLearner {
    private static final String M_PARAMETER = "m";

    private static final String PRIOR_PARAMETER = "prior probabilities";

    private static final String PRIOR_CLASS_PARAMETER = "class";

    private static final String PRIOR_PROBABILITY_PARAMETER = "probability";

    private static final double DEFAULT_M_VALUE = 0.0;

    public NaiveBayesMEstimate(OperatorDescription description) {
        super(description);
    }

    @Override
    public Model learn(ExampleSet exampleSet) throws OperatorException {
        return new NaiveBayesModel(
            exampleSet,
            getParameterAsDouble(M_PARAMETER),
            retrievePriorValues(exampleSet));
    }

    @Override
    public Class<? extends PredictionModel> getModelClass() {
        return super.getModelClass();
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        switch (capability) {
            case POLYNOMINAL_ATTRIBUTES:
            case BINOMINAL_ATTRIBUTES:
            case NUMERICAL_ATTRIBUTES:
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
            case WEIGHTED_EXAMPLES:
            case UPDATABLE:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        final List<ParameterType> types = super.getParameterTypes();

        types.add(new ParameterTypeDouble(
            M_PARAMETER,
            "This parameter defines the m value used when calculating the probability.",
            Double.MIN_VALUE,
            Double.MAX_VALUE,
            DEFAULT_M_VALUE
        ));

        types.add(new ParameterTypeList(
            PRIOR_PARAMETER,
            "Defines the a priori (estimated) probabilities of the individual classes.",
            new ParameterTypeString(
                    PRIOR_CLASS_PARAMETER,
                    "A possible class."
            ),
            new ParameterTypeDouble(
                    PRIOR_PROBABILITY_PARAMETER,
                    "The probability of the class.",
                    0,
                    1
            ),
            false
        ));

        return types;
    }

    private Map<Double, Double> retrievePriorValues(ExampleSet exampleSet) {
        try {
            final NominalMapping mapping = exampleSet.getAttributes().getLabel().getMapping();

            return getParameterList(PRIOR_PARAMETER).stream()
                    .collect(toMap(e -> (double)mapping.mapString(e[0]), e -> Double.valueOf(e[1])));
        } catch (Exception e) {
            LogService.getRoot().warning("The prior list parameter was undefined!");

            return new HashMap<>();
        }
    }
}
