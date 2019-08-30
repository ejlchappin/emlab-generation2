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

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.agent.NationalGovernment;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.market.CO2Auction;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.engine.Role;
import emlab.gen.engine.Schedule;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;
import java.util.logging.Level;

/**
 * {@link EnergyProducer}s pay CO2 taxes to the {@link Government}.
 *
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 * Chmieliauskas</a> @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile
 * Chappin</a>
 */
public class PayCO2AuctionRole extends AbstractEnergyProducerRole<EnergyProducer> implements Role<EnergyProducer> {

    public PayCO2AuctionRole(Schedule schedule) {
        super(schedule);
    }

    @Override
    public void act(EnergyProducer producer) {
        logger.info("Pay for the CO2 credits");

        Government government = getReps().government;

        for (PowerPlant plant : getReps().findOperationalPowerPlantsByOwner(producer, getCurrentTick())) {
            double money = calculateCO2MarketCost(plant, false, getCurrentTick());
            CashFlow cf = getReps().createCashFlow(producer, government, money, CashFlow.CO2AUCTION,
                    getCurrentTick(), plant);
            logger.log(Level.INFO, "Cash flow created: {0}", cf);
            double minCO2Money = calculatePaymentEffictiveCO2NationalMinimumPriceCost(plant, false, getCurrentTick());
            NationalGovernment nationalGovernment = getReps().findNationalGovernmentByPowerPlant(plant);
            CashFlow cf2 = getReps().createCashFlow(producer, nationalGovernment, minCO2Money,
                    CashFlow.NATIONALMINCO2, getCurrentTick(), plant);
            logger.log(Level.INFO, "Cash flow created: {0}", cf2);
        }

        CO2Auction auction = getReps().co2Auction;
        double co2Price = findLastKnownPriceOnMarket(auction, getCurrentTick());
        double deltaOfHedging = producer.getCo2Allowances() - producer.getLastYearsCo2Allowances();
        double money = co2Price * deltaOfHedging;
        if (money >= 0) {
            CashFlow cf2 = getReps().createCashFlow(producer, government, money,
                    CashFlow.CO2HEDGING, getCurrentTick(), null);
        } else {
            CashFlow cf2 = getReps().createCashFlow(government, producer, -money,
                    CashFlow.CO2HEDGING, getCurrentTick(), null);
        }

    }
}
