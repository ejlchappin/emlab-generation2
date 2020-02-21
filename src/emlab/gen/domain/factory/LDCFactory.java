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

import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.repository.Reps;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class LDCFactory extends AbstractFactory {

    public LDCFactory(Reps reps) {
        super(reps);
    }

    public void createSegments(double[] lengthsInHours) {

        for (int i = 0; i < lengthsInHours.length; i++) {
            createSegment(lengthsInHours[i]);
        }
    }

    public void createSegment(double lengthInHours) {

        Segment segment = new Segment();
        segment.setLengthInHours(lengthInHours);
        segment.setSegmentID(getReps().segments.size()+1);
        getReps().segments.add(segment);
        Logger.getGlobal().info("Created segment " + segment + " with id " + segment.getSegmentID() + " and length " + lengthInHours);

    }

    public Set<SegmentLoad> createLDC(double[] loads) {

        Set<SegmentLoad> ldc = new HashSet<>();

        for (int i = 0; i < loads.length; i++) {
            SegmentLoad segmentLoad = new SegmentLoad();
            segmentLoad.setBaseLoad(loads[i]);
//            segmentLoad.setElectricitySpotMarket(market); //TODO happens when creating the market!
            if(getReps().segments.size() <= i){
                createSegment(1d);
            }
            segmentLoad.setSegment(getReps().segments.get(i));
            ldc.add(segmentLoad);
            getReps().segmentLoads.add(segmentLoad);
        }
        return ldc;
    }

}
