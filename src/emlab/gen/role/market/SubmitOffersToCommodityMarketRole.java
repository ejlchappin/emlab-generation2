/*******************************************************************************
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
 ******************************************************************************/
package emlab.gen.role.market;

import emlab.gen.domain.agent.CommoditySupplier;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.CommodityMarket;
import emlab.gen.domain.market.DecarbonizationMarket;
import emlab.gen.engine.AbstractRole;
import emlab.gen.engine.Role;
import emlab.gen.engine.Schedule;

/**
 * {@link CommoditySupplier}s submit offers to the {@link CommodityMarket}s.
 * 
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas
 *         Chmieliauskas</a> @author <a
 *         href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 * 
 */
public class SubmitOffersToCommodityMarketRole extends AbstractRole<CommoditySupplier> implements Role<CommoditySupplier> {

    public SubmitOffersToCommodityMarketRole(Schedule schedule) {
        super(schedule);
    }

    public void act(CommoditySupplier supplier) {
        logger.finer("Submitting offers to commodity market");

        DecarbonizationMarket market = getReps().findMarketBySubstance(supplier.getSubstance());

        double price = supplier.getPriceOfCommodity().getValue(getCurrentTick());
        double amount = supplier.getAmountOfCommodity();

        Bid bid = getReps().submitBidToMarket(market, supplier, getCurrentTick(), true, price, amount);
        logger.finer("Submitted " + bid);
    }
}
