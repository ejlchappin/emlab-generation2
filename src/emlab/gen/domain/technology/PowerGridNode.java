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
package emlab.gen.domain.technology;


import emlab.gen.domain.gis.Zone;
import emlab.gen.trend.TimeSeriesImpl;

public class PowerGridNode {

    String name;

//    @RelatedTo(type = "REGION", elementClass = Zone.class, direction = Direction.OUTGOING)
    private Zone zone;

//    @RelatedTo(type = "HOURLYDEMAND", elementClass = HourlyCSVTimeSeries.class, direction = Direction.OUTGOING)
    private TimeSeriesImpl hourlyDemand;

    private double capacityMultiplicationFactor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimeSeriesImpl getHourlyDemand() {
        return hourlyDemand;
    }

    public void setHourlyDemand(TimeSeriesImpl hourlydemand) {
        this.hourlyDemand = hourlydemand;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public Zone getZone() {
        return zone;
    }

    public double getCapacityMultiplicationFactor() {
        return capacityMultiplicationFactor;
    }

    public void setCapacityMultiplicationFactor(double capacityMultiplicationFactor) {
        this.capacityMultiplicationFactor = capacityMultiplicationFactor;
    }

}
