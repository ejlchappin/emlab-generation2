#
# This is a Shiny web application of EMLab Generation 2. 
# You can run the application by clicking the 'Run App' button in RStudio or execute all of this file
#

library(shiny)

# Cleanup Environment first
rm(list=ls())

# Comment to always load latest reporters and logs
# Uncomment and specify to load specific reporters and logs
#id_to_load <- 1610700111414 


config_file <- "config.R"  
source(file = "app_scripts/init.R")

# Run the application (ui and server are in the folder emlab)
runApp("app") 
