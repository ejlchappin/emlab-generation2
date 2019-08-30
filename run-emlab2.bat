@echo off
set MODEL="emlab-generation2.jar"
set ITERATIONS=10
set PARALLEL=4
set SCENARIO="DefaultScenario"
set SCENARIO="Scenario_NL_DE"
set ROLE="EMlabModelRole"
set REPORTER="DefaultReporter"
set GUI="TRUE" 

cd dist
CALL java -jar %MODEL% gui=%GUI% iterations=%ITERATIONS% parallel=%PARALLEL% scenario=%SCENARIO% role=%ROLE% reporter=%REPORTER%
@pause