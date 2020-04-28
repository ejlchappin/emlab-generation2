



remove(list = ls())

# Config ------------------------------------------------------------------


config_file <- "config.R"
if(!file.exists(config_file)){
  stop("config.R not found. Please set up from example file")
} else {
  source(file = config_file)
  
}
library(tidyverse)
library(glue)
library(RColorBrewer)
library(DT)

if(use_plotly){
  library(plotly)
}



source(file = "emlab/util_functions.R")

# Loading and preparing files ---------------------------------------------

reporter_files <- get_result_files(emlab_results_directory)
log_files <- get_log_files(emlab_results_directory)

prefix_list <- reporter_files %>% 
  arrange(desc(date)) %>% 
  select(id, scenario) %>% 
  distinct() %>% 
  mutate(prefix = paste(id, scenario, sep ="-"))


files_to_analyse <- list()

# Get latest or selected one
if(exists("id_to_load")){
  files_to_analyse[["reporters"]] <- filter_selected_files(reporter_files, id_to_load)
  files_to_analyse[["log"]] <- filter_selected_files(log_files, id_to_load)
  file_loaded_text <- "Analysing config-file selected files: id = {id} and scenario = {scenario}."
  
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



# Get raw data ---------------------------------------------------------

raw_main_results <- read_emlab_results(files_to_analyse$reporters, "main.csv")

raw_marketinformation_results <- read_emlab_results(files_to_analyse$reporters, "MarketInformation.csv", 
  custom_col_types = cols(.default = "n", producer = "c", market = "c"))


# Read log files -----------------------------------------------------------

if(analyse_log){
  
  # save to rds file as parsing is slow
  log_rds_path <- paste0(emlab_results_directory,  id,".log.rds")
  
  if(file.exists(log_rds_path) & save_log_tempfile){
    message("Loading temp log file.")
    emlab_log <- readRDS(file = log_rds_path)
    
  } else {
    warning("Loading and parsing raw log file.")

    emlab_log <- read_emlab_csv_log(files_to_analyse$log) 
    
    if(save_log_tempfile){
      saveRDS(emlab_log, file = log_rds_path)
    }
    
  }

}



# Common variables --------------------------------------------------------

iteration_min <- min(raw_main_results$iteration)
iteration_max <- max(raw_main_results$iteration)

tick_expected_min <- min(raw_marketinformation_results$tick)
tick_expected_max <- max(raw_marketinformation_results$tick)



# Units
available_units <- tribble(
  ~name, ~prefix, ~factor,
  "Mega", "M", 1,
  "Giga", "G", 1/1000,
  "Tera", "T", 1/1000000)

all_unit_prefixes <- available_units$prefix
names(all_unit_prefixes) <- available_units$name


# Cleanup -----------------------------------------------------------------

# rm(
#   emlab_results_directory,
#   config_file,
#   get_filepath,
#   latest_files,
#   result_files,
#   get_latest_result_files,
#   get_result_files
# )



# Generate plots and data ----------------------------------------------------------

# Definition of data and plots 

data <- list()
plots <- list()
show_filters <- list()


# Load plots for different types of data
source(file = "emlab/data_main.R")
source(file = "emlab/data_marketinformation.R")


# theme for all ggplots
theme_set(
  theme_light(base_size = 13) +
  #theme_bw(base_size = 13) + 
    theme(
      #legend.title=element_blank(),
      legend.spacing.x = unit(0.1, 'cm')
    )
)


