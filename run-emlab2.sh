#!/bin/bash
MODEL="emlab-generation2.jar"
ITERATIONS=10
PARALLEL=4
#SCENARIO="DefaultScenario"
SCENARIO="Scenario_NL_DE"
ROLE="EMlabModelRole"
REPORTER="DefaultReporter"
GUI="FALSE" 


java -jar dist/$MODEL gui=$GUI iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER