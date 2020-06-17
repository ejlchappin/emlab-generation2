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
package emlab.gen.role.operating;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.market.CO2Auction;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.engine.Role;
import emlab.gen.engine.Schedule;
import emlab.gen.role.AbstractEnergyProducerRole;
import java.util.HashSet;
import java.util.logging.Level;

/**
 * Run the business. Buy supplies, pay interest, account profits
 *
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a> @author
 * <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas Chmieliauskas</a>
 * @author JCRichstein
 *
 */
public class DetermineFuelMixRole extends AbstractEnergyProducerRole<EnergyProducer> implements Role<EnergyProducer> {

    public DetermineFuelMixRole(Schedule schedule) {
        super(schedule);
    }

    public void act(EnergyProducer producer) {
        // get the co2 tax and market prices
        // CO2Auction market = getReps().genericRepository.findFirst(CO2Auction.class);
        // double co2AuctionPrice = findLastKnownPriceOnMarket(market);
        HashMap<ElectricitySpotMarket, Double> expectedCO2Prices = determineExpectedCO2PriceInclTax(getCurrentTick(), 1, getCurrentTick());
        Government government = getReps().government;
        // double co2TaxLevel = government.getCO2Tax(getCurrentTick());
        // logger.warn("Expected CO2 price: " + expectedCO2Prices.toString());

        for (PowerPlant plant : getReps().findOperationalPowerPlantsByOwner(producer, getCurrentTick())) {
            logger.log(Level.FINER, "Found operational power plant {0}", plant.getTechnology());

            // Fuels
            Set<Substance> possibleFuels = plant.getTechnology().getFuels();
            Map<Substance, Double> substancePriceMap = new HashMap<Substance, Double>();
            Set<SubstanceShareInFuelMix> fuelMix = new HashSet<SubstanceShareInFuelMix>();

            if (possibleFuels.size() > 0) {
                for (Substance substance : possibleFuels) {
                    substancePriceMap.put(substance, findLastKnownPriceForSubstance(substance, getCurrentTick()));
                }

                fuelMix = calculateFuelMix(plant, substancePriceMap,
                        expectedCO2Prices.get(getReps().findElectricitySpotMarketByPowerPlant(plant)));
            }
            plant.setFuelMix(fuelMix);

        }
    }

    public void determineFuelMixForecastForYearAndFuelPriceMap(long clearingTick,
            Map<Substance, Double> substancePriceMap, Map<ElectricitySpotMarket, Double> nationalMinCo2Prices) {

        CO2Auction co2Auction = getReps().co2Auction;
        double lastCO2Price;
        try {
            lastCO2Price = getReps().findClearingPointForMarketAndTime(co2Auction,
                    getCurrentTick() - 1, false).getPrice();
        } catch (NullPointerException e) {
            lastCO2Price = 0;
        }

        Government government = getReps().government;
        // double co2TaxLevel = government.getCO2Tax(getCurrentTick());

        for (ElectricitySpotMarket market : getReps().electricitySpotMarkets) {
            for (PowerPlant plant : getReps().findExpectedOperationalPowerPlantsInMarket(market,
                    clearingTick)) {
                logger.log(Level.FINER, "Found operational power plant {0}", plant.getTechnology());

                double effectiveCO2Price;

                if (nationalMinCo2Prices.get(market) > lastCO2Price) {
                    effectiveCO2Price = nationalMinCo2Prices.get(market);
                } else {
                    effectiveCO2Price = lastCO2Price;
                }

                effectiveCO2Price += government.getCO2Tax(clearingTick);
                // Fuels
                Set<Substance> possibleFuels = plant.getTechnology().getFuels();
                Map<Substance, Double> substancePriceMap1 = new HashMap<Substance, Double>();

                for (Substance substance : possibleFuels) {
                    substancePriceMap1.put(substance, findLastKnownPriceForSubstance(substance, getCurrentTick()));
                }
                Set<SubstanceShareInFuelMix> fuelMix = calculateFuelMix(plant, substancePriceMap1,
                        effectiveCO2Price);
                plant.setFuelMix(fuelMix);

            }
        }

    }

}
