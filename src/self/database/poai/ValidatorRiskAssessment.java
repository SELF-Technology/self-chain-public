package org.self.database.poai;

import org.self.objects.base.SELFData;

public class ValidatorRiskAssessment {
    private SELFData validatorId;
    private String behaviorRisk;
    private String crossChainRisk;
    
    public ValidatorRiskAssessment(
        SELFData validatorId,
        String behaviorRisk,
        String crossChainRisk
    ) {
        this.validatorId = validatorId;
        this.behaviorRisk = behaviorRisk;
        this.crossChainRisk = crossChainRisk;
    }
    
    public SELFData getValidatorId() {
        return validatorId;
    }
    
    public String getBehaviorRisk() {
        return behaviorRisk;
    }
    
    public String getCrossChainRisk() {
        return crossChainRisk;
    }
}
