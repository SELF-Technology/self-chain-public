package org.self.database.poai;

import org.self.objects.base.SELFData;

public class ValidatorAnalysis {
    private SELFData validatorId;
    private String behaviorAnalysis;
    private String crossChainAnalysis;
    
    public ValidatorAnalysis(
        SELFData validatorId,
        String behaviorAnalysis,
        String crossChainAnalysis
    ) {
        this.validatorId = validatorId;
        this.behaviorAnalysis = behaviorAnalysis;
        this.crossChainAnalysis = crossChainAnalysis;
    }
    
    public SELFData getValidatorId() {
        return validatorId;
    }
    
    public String getBehaviorAnalysis() {
        return behaviorAnalysis;
    }
    
    public String getCrossChainAnalysis() {
        return crossChainAnalysis;
    }
}
