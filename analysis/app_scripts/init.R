
# Config ------------------------------------------------------------------

if(is.null(config_file)){
  message("No config file loaded, manually define config variables.")
} else if(!file.exists(config_file)){
  stop("config.R not found. Please set up from example file")
} else {
  source(file = config_file)
}


library(tidyverse)
library(glue)
library(RColorBrewer)
library(DT)

source(file = "app_scripts/util_functions.R")

# Loading and preparing files ---------------------------------------------

reporter_files <- get_result_files(config_params[["emlab_results_directory"]])
log_files <- get_log_files(config_params[["emlab_results_directory"]])

prefix_list <- reporter_files %>% 
  arrange(desc(date)) %>% 
  select(id, scenario) %>% 
  distinct() %>% 
  mutate(prefix = paste(id, scenario, sep ="-"))

files_to_analyse <- list()

# Get latest or selected one
if(exists("id_to_load")){
  if(!is.na(id_to_load)){
    files_to_analyse[["reporters"]] <- filter_selected_files(reporter_files, id_to_load)
    files_to_analyse[["log"]] <- filter_selected_files(log_files, id_to_load)
    file_loaded_text <- "Analysing config-file selected files: id = {id} and scenario = {scenario}."
  } else {
    stop("id_to_load is NA. Please set in id_to_load or uncomment to load latest.")
  }

} else {
  files_to_analyse[["reporters"]] <- filter_latest_files(reporter_files)
  files_to_analyse[["log"]] <- filter_latest_files(log_files)
  file_loaded_text <- "Analysing latest files: id = {id} and scenario = {scenario}."
  
}

id <- files_to_analyse$reporters %>% 
  slice(1) %>% 
  pull(id)

scenario <- files_to_analyse$reporters %>% 
  slice(1) %>% 
  pull(scenario)

prefix = paste(id, scenario, sep = "-")
message(glue(file_loaded_text))

# Read log files -----------------------------------------------------------

if(config_params[["analyse_log"]]){
  
  # save to rds file as parsing is slow
  log_rds_path <- paste0(config_params[["emlab_results_directory"]],  id,".log.rds")
  
  if(file.exists(log_rds_path) & config_params[["save_log_tempfile"]]){
    message("Loading temp log file.")
    emlab_log <- readRDS(file = log_rds_path)
    
  } else {
    warning("Loading and parsing raw log file.")

    emlab_log <- read_emlab_csv_log(files_to_analyse$log) 
    
    if(config_params[["save_log_tempfile"]]){
      saveRDS(emlab_log, file = log_rds_path)
    }
    
  }

} else {
  message("No log-files are processed")
}


# Read description file ---------------------------------------------------

description_filename <- "scenario_informations.csv"
description_file <- file.path(config_params[["emlab_results_meta_directory"]], description_filename)
scenario_descriptions <- load_description_file(description_file)

scenario_descriptions_initial_name <- scenario_descriptions %>% filter(prefix == !!prefix) %>% pull(file_scenario_name)
scenario_descriptions_initial_caption <- scenario_descriptions %>% filter(prefix == !!prefix) %>% pull(file_scenario_caption)

# Common variables --------------------------------------------------------

# Units
available_units <- tribble(
  ~name, ~prefix, ~factor,
  "Mega", "M", 1,
  "Giga", "G", 1/1000,
  "Tera", "T", 1/1000000)

all_unit_prefixes <- available_units$prefix
names(all_unit_prefixes) <- available_units$name


# Load files and data and generate plots  ----------------------------------------------------------

# Definition of data and plots 

data <- list()
plots <- list()
show_filters <- list()

# these data needs to be processes because several filters take values from them
process_data_required <- c("operational_capacities", "cash_by_producers", "fuel_prices", "segment_prices")

# Source the data
for(this_data_to_load in config_params[["data_to_load"]]){
  
  this_data_to_load_file <- file.path("app_plots", glue("data_{this_data_to_load}.R"))
  if(file.exists(this_data_to_load_file)){
    message(glue("Now loading reporter file: {this_data_to_load_file}"))
    source(file = this_data_to_load_file)
  } else {
    stop(glue("data file {this_data_to_load_file} does not exist"))
  }
  
  # check and load custom plots
  this_data_to_load_file <- file.path("custom_plots", glue("data_{this_data_to_load}.R"))
  if(file.exists(this_data_to_load_file)){
    message(glue("Now loading custom plots: {this_data_to_load_file}"))
    source(file = this_data_to_load_file)
  }
  
}


# Other -------------------------------------------------------------------



# theme for all ggplots
theme_set(
  theme_light(base_size = 13) +
  #theme_bw(base_size = 13) + 
    theme(
      #legend.title=element_blank(),
      legend.spacing.x = unit(0.1, 'cm')
    )
)

# Cleanup -----------------------------------------------------------------

# rm(
#   config_params[["emlab_results_directory"]],
#   config_file,
#   get_filepath,
#   latest_files,
#   result_files,
#   get_latest_result_files,
#   get_result_files
# )




