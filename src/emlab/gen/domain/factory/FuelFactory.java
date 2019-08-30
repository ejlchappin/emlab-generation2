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
package emlab.gen.domain.factory;

import emlab.gen.domain.agent.CommoditySupplier;
import emlab.gen.domain.market.CommodityMarket;
import emlab.gen.domain.technology.Substance;
import emlab.gen.repository.Reps;
import emlab.gen.trend.TimeSeriesImpl;

public class FuelFactory extends AbstractFactory {

    public FuelFactory(Reps reps) {
        super(reps);
    }

    public Substance createFuel(String name, double cO2Density, double energyDensity, double quality, TimeSeriesImpl price){
        
        Substance fuel = new Substance();
        fuel.setName(name);
        fuel.setCo2Density(cO2Density);
        fuel.setEnergyDensity(energyDensity);
        fuel.setQuality(quality);
                   
        createCommodityMarket(name + " market", fuel, false);
        createCommoditySupplier(name + " supplier", fuel, price);
        
        getReps().substances.add(fuel);
        getReps().substancesOnCommodityMarkets.add(fuel);
            
        return fuel;
    }

    public CommodityMarket createCommodityMarket(String name, Substance substance, boolean isAuction) {
        CommodityMarket market = new CommodityMarket();
        market.setName(name);
        market.setSubstance(substance);
        market.setAuction(isAuction);
        getReps().commodityMarkets.add(market);
        getReps().marketForSubstance.put(substance, market);
        return market;
    }

    public CommoditySupplier createCommoditySupplier(String name, Substance substance, TimeSeriesImpl price) {
        CommoditySupplier commoditySupplier = new CommoditySupplier();
        commoditySupplier.setName(name);
        commoditySupplier.setSubstance(substance);
        commoditySupplier.setPriceOfCommodity(price);
        getReps().commoditySuppliers.add(commoditySupplier);
        return commoditySupplier;
    }
}
