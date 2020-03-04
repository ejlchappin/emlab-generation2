/** *****************************************************************************
 * Copyright 2012 the original author or authors.
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
package emlab.gen.role;

import emlab.gen.domain.agent.EMLabModel;
import emlab.gen.domain.technology.Substance;
import emlab.gen.engine.AbstractRole;
import emlab.gen.engine.Role;
import emlab.gen.engine.Schedule;
import java.util.logging.Level;

/**
 * Main model role.
 *
 * @author ejlchappin
 *
 */
public class EmptyRoleBeginning extends AbstractRole<EMLabModel> implements Role<EMLabModel> {

    public EmptyRoleBeginning(Schedule schedule) {
        super(schedule);
    }

    /**
     * Main model script. Executes other roles in the right sequence.
     */
    @Override
    public void act(EMLabModel model) {

        //This is example behavior that can be added in the beginning to make later changes to a scenario.
//        Substance gas = getReps().findSubstanceByName("naturalgas");
//        if(getCurrentTick() == 5){
//            logger.warning("I'm increasing the CO2 density of gas from " + gas.getCo2Density());
//            gas.setCo2Density(gas.getCo2Density()*1.5);
//            logger.warning("I'm increasing the CO2 density of gas to " + gas.getCo2Density());   
//        }
    }

}
