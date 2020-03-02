/** *****************************************************************************
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
package emlab.gen.role.investment;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.engine.Role;
import emlab.gen.engine.Schedule;

/**
 * {@link EnergyProducer}s decide to invest in new {@link PowerPlant}
 *
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a> @author
 * <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas Chmieliauskas</a>
 * @author JCRichstein
 * @author marcmel
 */
public class InvestInPowerGenerationTechnologiesRole<T extends EnergyProducer> extends AbstractInvestInPowerGenerationTechnologiesRole<T> implements Role<T> {

    public InvestInPowerGenerationTechnologiesRole(Schedule schedule) {
        super(schedule);
    }


    public void act(T agent) {
    	
    	initEvaluationForEnergyProducer(agent, agent.getInvestorMarket());
    	
        PowerPlant bestPlant = null;
        double highestValue = Double.MIN_VALUE;

        for (PowerGeneratingTechnology technology : getReps().powerGeneratingTechnologies) {

            PowerPlant plant = createPowerPlant(technology);
            FutureCapacityExpectation futureCapacityExpectation = new FutureCapacityExpectation(technology, plant);
            
            if(futureCapacityExpectation.isViableInvestment()) {
                
            	FutureFinancialExpectation financialExpectation = new FutureFinancialExpectation(plant);

                if (financialExpectation.plantHasRequiredRunningHours()) {
                	financialExpectation.calculateDiscountedValues();
                    

                    // Divide by capacity, in order not to favour large power plants (which have the single largest NP)
                    if (financialExpectation.getProjectValue() > 0 && financialExpectation.getProjectValue() / plant.getActualNominalCapacity() > highestValue) {
                        highestValue = financialExpectation.getProjectValue() / plant.getActualNominalCapacity();
                        bestPlant = plant;
                    }
                }
            }
        }
        
        decideToInvestInPlant(bestPlant);
        
    }

}
