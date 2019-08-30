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
package emlab.gen.role.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import emlab.gen.domain.agent.EMLabModel;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.engine.Role;
import emlab.gen.engine.Schedule;
import emlab.gen.role.AbstractEnergyProducerRole;

/**
 * {@link EnergyProducer} submits offers to the {@link ElectricitySpotMarket}.
 * One {@link Bid} per {@link PowerPlant}.
 *
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 * Chmieliauskas</a> @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile
 * Chappin</a>
 *
 */
public class SubmitOffersToElectricitySpotMarketRole extends AbstractEnergyProducerRole<EnergyProducer> implements
        Role<EnergyProducer> {

    public SubmitOffersToElectricitySpotMarketRole(Schedule schedule) {
        super(schedule);
    }

    public void act(EnergyProducer producer) {
        createOffersForElectricitySpotMarket(producer, getCurrentTick(), false, null);
    }

    public void createOffersForElectricitySpotMarket(EnergyProducer producer, long tick,
            boolean forecast, Map<Substance, Double> forecastedFuelPrices) {
        List<PowerPlantDispatchPlan> ppdpList = new ArrayList<PowerPlantDispatchPlan>();

        if (forecastedFuelPrices == null && !forecast) {
            EMLabModel model = getReps().emlabModel;
            forecastedFuelPrices = new HashMap<Substance, Double>();
            for (Substance substance : getReps().substancesOnCommodityMarkets) {
                forecastedFuelPrices.put(substance, findLastKnownPriceForSubstance(substance, getCurrentTick()));
            }
        }

        long numberOfSegments = getReps().segments.size();
        ElectricitySpotMarket market = null;
        if (producer != null) {
            market = producer.getInvestorMarket();
        }

        Iterable<PowerPlant> powerPlants;
        if (producer != null) {
            powerPlants = forecast ? getReps()
                    .findExpectedOperationalPowerPlantsInMarketByOwner(market, tick, producer) : getReps()
                    .findOperationalPowerPlantsByOwner(producer, tick);
        } else {
            powerPlants = forecast ? getReps().findExpectedOperationalPowerPlants(tick)
                    : getReps().findOperationalPowerPlants(tick);
        }

        boolean producerIsNull = (producer == null) ? true : false;
        // find all my operating power plants
        for (PowerPlant plant : powerPlants) {

            if (producerIsNull) {
                market = getReps().findElectricitySpotMarketForZone(plant.getLocation().getZone());
                producer = plant.getOwner();
            }

            double mc;
            double price;
            if (!forecast) {
                mc = calculateMarginalCostExclCO2MarketCost(plant, tick);
                price = mc * producer.getPriceMarkUp();
            } else {
                mc = calculateExpectedMarginalCostExclCO2MarketCost(plant, forecastedFuelPrices, tick);
                price = mc * producer.getPriceMarkUp();
            }

            logger.info("Submitting offers for " + plant.getName() + " with technology " + plant.getTechnology().getName());

            List<PowerPlantDispatchPlan> plans = getReps().findPowerPlantDispatchPlansForPowerPlantForTime(plant, tick, forecast);

            for (SegmentLoad segmentload : market.getLoadDurationCurve()) {
                Segment segment = segmentload.getSegment();
                double capacity;
                if (tick == getCurrentTick()) {
                    capacity = plant.getAvailableCapacity(tick, segment, numberOfSegments);
                } else {
                    capacity = plant.getExpectedAvailableCapacity(tick, segment, numberOfSegments);
                }

                logger.info("I bid capacity: " + capacity + " and price: " + mc);

                PowerPlantDispatchPlan plan = plans.stream().filter(p -> (p.getSegment().equals(segment))).findFirst().orElse(getReps().createPowerPlantDispatchPlan(plant, producer, market, segment, tick, price, price, capacity, 0,
                        Bid.SUBMITTED, forecast));

                // plan = plans.iterator().next();
                plan.setBidder(producer);
                plan.setBiddingMarket(market);
                plan.setPrice(mc);
                plan.setBidWithoutCO2(mc);
                plan.setAmount(capacity);
                plan.setAcceptedAmount(0d);
                plan.setCapacityLongTermContract(0d);
                plan.setStatus(Bid.SUBMITTED);
                plan.setForecast(forecast);

                ppdpList.add(plan);

            }

        }

        // return ppdpList;
    }

    void updateMarginalCostInclCO2AfterFuelMixChange(double co2Price,
            Map<ElectricitySpotMarket, Double> nationalMinCo2Prices, long clearingTick, boolean forecast,
            Map<Substance, Double> fuelPriceMap) {

        int i = 0;
        int j = 0;

        Government government = getReps().government;
        for (PowerPlantDispatchPlan plan : getReps()
                .findAllPowerPlantDispatchPlansForTime(
                        clearingTick, forecast)) {
            j++;

            double effectiveCO2Price;

            double capacity = plan.getAmount();
            if (nationalMinCo2Prices.get(plan.getBiddingMarket()) > co2Price) {
                effectiveCO2Price = nationalMinCo2Prices.get(plan.getBiddingMarket());
            } else {
                effectiveCO2Price = co2Price;
            }

            if (plan.getPowerPlant().getFuelMix().size() > 1) {

                double oldmc = plan.getBidWithoutCO2();

                // Fuels
                Set<Substance> possibleFuels = plan.getPowerPlant().getTechnology().getFuels();
                Map<Substance, Double> substancePriceMap = new HashMap<Substance, Double>();

                for (Substance substance : possibleFuels) {
                    substancePriceMap.put(substance, fuelPriceMap.get(substance));
                }
                Set<SubstanceShareInFuelMix> fuelMix = calculateFuelMix(plan.getPowerPlant(), substancePriceMap,
                        government.getCO2Tax(clearingTick) + effectiveCO2Price);
                plan.getPowerPlant().setFuelMix(fuelMix);
                double mc = calculateMarginalCostExclCO2MarketCost(plan.getPowerPlant(), clearingTick);
                if (mc != oldmc) {
                    plan.setBidWithoutCO2(mc);
                    i++;
                }

            }

            plan.setPrice(plan.getBidWithoutCO2()
                    + (effectiveCO2Price * plan.getPowerPlant().calculateEmissionIntensity()));

            plan.setStatus(Bid.SUBMITTED);
            plan.setAmount(capacity);
            plan.setAcceptedAmount(0d);
            plan.setCapacityLongTermContract(0d);

        }

        //logger.warn("Marginal cost of {} of {} plans changed", i, j);
    }

}
