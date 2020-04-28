
# Install -----------------------------------------------------------------
# Make sure the following packages are installed

install.packages("tidyverse")
install.packages("shiny")
install.packages("shinyjs")
install.packages("shinydashboard")
install.packages("DT")
install.packages("RColorBrewer")
install.packages("plotly")


if(!file.exists("config.R")){
  warning("Now, you have to create config.R from example_config.R")
}