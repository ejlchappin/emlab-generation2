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
package emlab.gen.domain.factory;

import java.io.InputStreamReader;
import java.util.List;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author JCRichstein
 *
 */
public class PowerPlantCSVFactory extends AbstractFactory {

    
    String csvFile;

    static final Logger logger = Logger.getGlobal();


    public PowerPlantCSVFactory(Reps reps) {
        super(reps);
    }

    public List<PowerPlant> read() {
        logger.log(Level.WARNING, "Reading power plant from CSV file: {0}", csvFile);
        List<PowerPlant> powerplants = null;
        InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream(csvFile));

        CSVReader<PowerPlant> csvPersonReader = new CSVReaderBuilder<PowerPlant>(inputStreamReader).entryParser(
                new PowerPlantEntryParser(getReps().energyProducers, getReps().powerGeneratingTechnologies, getReps().powerGridNodes))
                .strategy(new CSVStrategy(',', '\"', '#', true, true))
                .build();
        try {
            powerplants = csvPersonReader.readAll();
        } catch (IOException ex) {
            Logger.getLogger(PowerPlantCSVFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return powerplants;
    }

    public String getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(String csvFile) {
        this.csvFile = csvFile;
    }



}
