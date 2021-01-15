# EMLab-Generation2

A second evolution of the EMLab-Generation Agent-based model

## Abstract
EMLab is a suite of Agent-Based Models dealing with policy questions on the long-term evolution of the electricity sector. It contains various scenarios and agent behaviour that allows the user to capture developments in this sector. The research using this model focuses on CO2 and renewables policy, generation adequacy, capacity mechanisms, investment risk and behaviour and open data. EMLab-Generation is a standalone java application that can be run in Netbeans. 

These instructions will allow you to install the model, run it, vary scenarios and visualize the outcomes. 

## Download and install the model and Netbeans
*	Download the model from https://github.com/ejlchappin/emlab-generation2, click ‘Code’, download ZIP.
*	Unzip it to a location you want to have the model.
*	Download and install Java JDK from https://www.oracle.com/nl/java/technologies/javase-jdk15-downloads.html 
*	Download Netbeans from http://netbeans.apache.org/, download version 12, download under ‘binaries’ and download as zip. Unzip the download to a location where you want the software
*	You start netbeans from the bin folder (netbeans64.exe for Windows or netbeans for Mac OS).
*	Open the model as a Netbeans project (under ‘File / Open project’).

## Run the model
*	You can run the model from Netbeans with the green play button, or under ‘Run / Run project – emlag-generation2’.

Note: it is possible to make a compiled version and run it without Netbeans, when java is properly installed. 

## Selecting scenario and making changes
The default scenario is selected when you open the model the first time. All code can be found under ‘source packages’. Data are under ‘resources / data’.

You can change some defaults settings for running the simulation by opening the file under the package ‘emlab.gen.engine’ with the name ‘Startup.java’. Under the line: *Default parameters*. You can modify the number of runs that are performed (default 10), how many are executed in parallel (default 4), and the scenario that is run (default 'DefaultScenario'). 

The scenarios are located under the package ‘emlab.gen.scenarios’. The model also comes with a scenario covering the Netherlands and one with The Netherlands and Germany, resp. ‘Scenario_NL’ and ‘Scenario_NL_DE’ and a scenario with improved representation of intermittent renewables 'Scenario_NL_intermittent'. There also is a quick toy scenario 'Scenario_NL_DE_toy'. The de facto standard scenario at the moment is 'Scenario_NL_intermittent'.

To make your own scenarios, copy one of the scenarios and start modifying by opening the file and changing parameters. You can change many things simply by altering the scenario file. From top to bottom it covers the following parameters:
*	The length of the simulation (in years)
*	Whether CO2 emissions trading is implemented
*	Electricity demand growth
*	Fuel price trends
*	Whether you have one or two countries/markets. In case you have two, also the interconnector capacity (in MW)
*	The load duration curve for each country. The LDC data is in a separate file which is referred to in the scenario. For instance, for the Netherlands, the reference is "/data/ldcNL.csv". If you would like to change this, make a copy of the data file, change the file, and refer to the new filename in the scenario. 
*	Power generation company agents and their properties and preferences
*	A CO2 tax, how the CO2 cap changes over time, a minimum CO2-price
*	The properties for power generating technologies and how they change over time
*	Power plants at the start of the simulation. As for the LDC, the data is in a separate file, referred to in the scenario. If you would like to change this, make a copy of the file, change the file, and refer to the new filename in the scenario. The file for the Dutch/German scenario, for instance, is here: "/data/dutchGermanPlants2015.csv" The initial power plant portfolio is defined by setting the share of different technologies in the installed capacity market of by using a comma separated value. The information of these power plants are: name, technology, location, age, owner, capacity, efficiency. 

## Selecting scenario and making changes
The default scenario is selected when you open the model the first time. All code can be found under ‘source packages’. Data are under ‘resources / data’.

You can change some defaults settings for running the simulation by opening the file under the package ‘emlab.gen.engine’ with the name ‘Startup.java’. Under the line: *Default parameters*. You can modify the number of runs that are performed (default 10), how many are executed in parallel (default 4), and the scenario that is run (default DefaultScenario). 

*The scenarios* are located under the package ‘emlab.gen.scenarios’. The model also comes with a scenario covering the Netherlands and one with The Netherlands and Germany, resp. ‘Scenario_NL’ and ‘Scenario_NL_DE’.

To make your own scenarios, copy one of the scenarios and start modifying by opening the file and changing parameters. You can change many things simply by altering the scenario file. From top to bottom it covers the following parameters:
*	The length of the simulation (in years)
*	Whether CO2 emissions trading is implemented
*	Electricity demand growth
*	Fuel price trends
*	Whether you have one or two countries/markets. In case you have two, also the interconnector capacity (in MW)
*	The load duration curve for each country. The LDC data is in a separate file which is referred to in the scenario. For instance, for the Netherlands, the reference is "/data/ldcNL.csv". If you would like to change this, make a copy of the data file, change the file, and refer to the new filename in the scenario. 
*	Power generation company agents and their properties and preferences
*	A CO2 tax, how the CO¬2 cap changes over time, a minimum CO2 ¬price
*	The properties for power generating technologies and how they change over time
*	Power plants at the start of the simulation. As for the LDC, the data is in a separate file, referred to in the scenario. If you would like to change this, make a copy of the file, change the file, and refer to the new filename in the scenario. The file for the Dutch/German scenario, for instance, is here: "/data/dutchGermanPlants2015.csv"

## Analyze results
Each time the model is run the results are saved as text files. The files start with a unique long sequence of numbers. Newer runs will have a higher number, so you know which is the most recent. These files are created:
*	DefaultReporter-Main: A semi-column separated file with key statistics of the simulation run. This is the key output.
*	DefaultReporter-Powerplants: A semi-column separated file with an overview of all power plants during the simulation run. This helps to understand results by diving into specifics of individual power plants.
*	DefaultReporter-MarketInformation: A semi-column separated file with an overview of what agents believe/predict in the future electricity market. This can help to understand investment patterns, because you can see the information the agents based their decisions on.
*	DefaultReporter-Segments: A semi-column separated file with an overview of the shape of the load duration curve. This is a dynamically altered set of segments with varying duration and load. This makes it hard to compare segments between simulation runs and over time. In order to help that analysis, all the information is contained here.
*	Log: A textual log of all text that is printed.

This R shiny app provides an UI to analyse EMLab Generation output. With it is possible to:
*	Analyse runs from EMLab with a graphical and interactive interface
*	Search through the log files
*	Generate custom reports for further use in publications

In order to run the analysis package:
*	Download and install R statistics (https://www.r-project.org/). Download and install R Studio (https://www.rstudio.com/).
*	Open R Studio and open under the folder analysis, the file install.R. Run this code, it will trigger the install of all required packages.  
*	Open the file app.R and run the code within (do not click ‘Run app’, but select the code in the file and press cntrl/command enter. If you have results, the app will show in your browser. By default, the app opens the most recent simulation run. You can customise this behaviour by setting id_to_load in app.R (or alternatively config.R).

## Datasources

We use the following datasources in EMLab-Generation2
* Hourly capacity factors for PV, onshore wind and offshore wind from www.renewables.ninja by Staffell and Pfenninger (2016)

Sources:
I. Staffell and S. Pfenninger, 2016. Using Bias-Corrected Reanalysis to Simulate Current and Future Wind Power Output. Energy, 114, 1224–1239. http://dx.doi.org/10.1016/j.energy.2016.08.068

## Further reading
An earlier version of the model is published open source can be used under the licence as described with the source code. In this course, a new version is embedded, which is not yet published online. Please do not distribute.
EMLab-generation is described in detail in this paper (Chappin et al., 2017):

*	Chappin, E. J. L., Vries, L. J. de, Richstein, J. C., Bhaghwat, P., Iychettira, K., & Khan, S. (2017). Simulating climate and energy policy with agent-based modelling: the Energy Modelling Laboratory (EMLab). Environmental Modelling & Software, 96, 421–431. https://doi.org/10.1016/j.envsoft.2017.07.009. https://www.sciencedirect.com/science/article/pii/S1364815216310301

And in this report (De Vries et al., 2013):
*	De Vries, L.J., Chappin, E.J.L., Richstein, J.C., 2013. EMLab-Generation - An experimentation environment for electricity policy analysis. TU Delft. http://emlab.tudelft.nl/generation/emlab-generation-report-1.2.pdf

EMLab-Generation has, amongst others been used in the following publications:
*	Bhagwat, P.C., Richstein, J.C., Chappin, E.J.L., Vries, L.J. de, 2016. The effectiveness of a strategic reserve in the presence of a high portfolio share of renewable energy sources. Util. Policy 39, 13–28. doi:http://dx.doi.org/10.1016/j.jup.2016.01.006
*	Richstein, J.C., 2015. Interactions between carbon and power markets in transition. Gildeprint Drukkerijen. doi:10.4233/uuid:0e1dcc59-40f0-4ff9-a330-c185fdfca119
*	Richstein, J.C., Chappin, E.J.L., Vries, L.J. de, 2015a. The Market (In-)Stability Reserve for EU Carbon Emission Trading: Why it may fail and how to improve it. Util. Policy 35, 1–18. doi:10.1016/j.jup.2015.05.002
*	Richstein, J.C., Chappin, E.J.L., Vries, L.J. de, 2015b. Adjusting the CO2 cap to subsidised RES generation: Can CO2 prices be decoupled from renewable policy? Appl. Energy 156, 693–702. doi:10.1016/j.apenergy.2015.07.024
*	Richstein, J.C., Chappin, E.J.L., Vries, L.J. de, 2014. Cross-border electricity market effects due to price caps in an emission trading system: An agent-based approach. Energy Policy 71, 139–158. doi:10.1016/j.enpol.2014.03.037
