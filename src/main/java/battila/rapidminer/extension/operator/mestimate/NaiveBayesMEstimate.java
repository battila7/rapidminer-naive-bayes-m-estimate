package battila.rapidminer.extension.operator.mestimate;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.PredictionModel;

public class NaiveBayesMEstimate extends AbstractLearner {
    public NaiveBayesMEstimate(OperatorDescription description) {
        super(description);
    }

    @Override
    public Model learn(ExampleSet exampleSet) throws OperatorException {
        return null;
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
}
