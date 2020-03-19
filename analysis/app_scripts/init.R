
library(tidyverse)
library(glue)
library(RColorBrewer)


# Config ------------------------------------------------------------------


config_file <- "config.R"
if(!file.exists(config_file)){
  stop("config.R not found. Please set up from example file")
} else {
  source(file = config_file)
  
}

source(file = "app_scripts/util_functions.R")

# Loading and preparing files ---------------------------------------------

result_files <- get_result_files(emlab_results_directory)
latest_files <- get_latest_result_files(result_files)
  
# TODO selection, maybe in Shiny app
#id = "1583329851748"
#scenario = "Scenario_NL"

id <- latest_files %>% 
  slice(1) %>% 
  pull(id)

scenario <- latest_files %>% 
  slice(1) %>% 
  pull(scenario)

warning("Analysing latest result files")
prefix = paste(id, scenario, sep = "-") 


# Get raw results ---------------------------------------------------------

raw_main_results <- read_results("main.csv")
raw_marketinformation_results <- read_results(
  "MarketInformation.csv", 
  custom_col_types = cols(.default = "n", producer = "c", market = "c"))


# Common variables --------------------------------------------------------

iteration_min <- min(raw_main_results$iteration)
iteration_max <- max(raw_main_results$iteration)

# Units
available_units <- tribble(
  ~name, ~prefix, ~factor,
  "Mega", "M", 1,
  "Giga", "G", 1/1000,
  "Tera", "T", 1/1000000)

all_unit_prefixes <- available_units$prefix
names(all_unit_prefixes) <- available_units$name


# Cleanup -----------------------------------------------------------------

rm(
  emlab_results_directory,
  config_file,
  get_filepath,
  latest_files,
  result_files,
  get_latest_result_files,
  get_result_files
)



# Generate plots and data ----------------------------------------------------------

# Definition of data and plots 

data <- list()
plots <- list()


# Load plots for different types of data
source(file = "app_scripts/data_main.R")
source(file = "app_scripts/data_marketinformation.R")



