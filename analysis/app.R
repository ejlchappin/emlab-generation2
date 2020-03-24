#
# This is a Shiny web application of EMLab Generation 2. 
# You can run the application by clicking the 'Run App' button above.
#

# Install -----------------------------------------------------------------
# Make sure the following packages are installed

#install.packages("tidyverse)
#install.packages("shiny")
#install.packages("shinyjs")
#install.packages("shinydashboard")


remove(list = ls())

library(shiny)
library(shinyjs)

# In the init.R all results are read and common variables are prepared
source(file = "app_scripts/init.R")

# Run the application (ui and server are in the folder emlab)
runApp("emlab")