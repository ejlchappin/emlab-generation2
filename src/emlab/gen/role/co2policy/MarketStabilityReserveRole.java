/*******************************************************************************
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
 ******************************************************************************/
package emlab.gen.role.co2policy;

import emlab.gen.domain.agent.Government;
import emlab.gen.engine.AbstractRole;
import emlab.gen.engine.Schedule;

/**
 * @author JCRichstein
 *
 */
public class MarketStabilityReserveRole extends AbstractRole<Government> {

    public MarketStabilityReserveRole(Schedule schedule) {
        super(schedule);
    }

    public void act(Government government) {
        double allowancesInCirculation = government.isStabilityReserveHasOneYearDelayInsteadOfTwoYearDelay() ? getReps().determineTotallyBankedCO2Certificates() : getReps().determinePreviouslyBankedCO2Certificates();
        double inflowToMarketReserve = calculateInflowToMarketReserveForTimeStep(getCurrentTick(),
                allowancesInCirculation, government);
        government.setStabilityReserve(government.getStabilityReserve() + inflowToMarketReserve);
        government.getCo2CapTrend().setValue(getCurrentTick(),
                government.getCo2CapTrend().getValue(getCurrentTick()) - inflowToMarketReserve);
    }

    public double calculateInflowToMarketReserveForTimeStep(long clearingTick, double bankedCertificatesInTick,
            Government government) {
        double allowancesInCirculation = bankedCertificatesInTick;
        if (allowancesInCirculation > government.getStabilityReserveUpperTriggerTrend().getValue(clearingTick)) {
            double allowancesToBeAddedToReserve = Math.max(
                    allowancesInCirculation
                    * government.getStabilityReserveAddingPercentageTrend().getValue(clearingTick),
                    government
                    .getStabilityReserveAddingMinimumTrend().getValue(clearingTick));
            return allowancesToBeAddedToReserve;
        } else if (allowancesInCirculation < government.getStabilityReserveLowerTriggerTrend().getValue(clearingTick)) {
            double allowancesToBeReleased = Math.min(government.getStabilityReserve(),
                    government
                    .getStabilityReserveReleaseQuantityTrend().getValue(clearingTick));
            return -allowancesToBeReleased;
        }
        return 0;
    }
}
