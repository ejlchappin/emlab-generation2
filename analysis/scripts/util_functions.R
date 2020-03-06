
## General util functions to load files and parse the dataformat used in EMLAB
## @author marcmel

# Files -------------------------------------------------------------------


#' Get a list of all results csv files from the results folder
#' The filenames need to follow the following scheme and use "-" as separator
#' "id", "scenario", "model", "reporter", "datatype"
#'
#' @param directory path to results files
#'
#' @return a tibble() of all result files
#' @export
#'
#' @examples
get_result_files <- function(directory){
  
  file_list <- list.files(path = directory, pattern = "*.csv")
  
  tibble(
    file = file_list,
    date = file.info(paste0(directory,file_list))$ctime
  ) %>% 
    arrange(date) %>% 
    separate(col = "file", into = c("id", "scenario", "model", "reporter", "datatype"), sep = "-", remove = FALSE)
  
}


get_latest_result_files <- function(result_files){
  
  lastest_results_id <- result_files %>% 
    select(id, date) %>% 
    filter(date == max(date)) %>% 
    slice(1) %>% 
    pull(id)
  
  result_files %>%
    filter(id == lastest_results_id)
} 

#' Get specific file of the current scenario and id. 
#'
#' @param datafile the name  of the .csv, e.g. "main.csv"
#'
#' @return a filename
#' @export
#'
#' @examples
get_filepath <- function(datafile){
  
  filename <- latest_files %>% 
    filter(datatype == datafile) %>% 
    pull(file);
  
  paste0(emlab_results_directory,filename)
  
}




# Plots -------------------------------------------------------------------

geom_area_shaded <- function(){
  geom_area(colour = "black", size = 0.2, alpha = 0.6)
}
scale_fill_technologies <- function(){
  scale_fill_brewer(type = "qual", palette = "Set3")
}


# Data parsing ------------------------------------------------------------

get_data_by_prefix <- function(data, col_prefix, value){
  
  col_prefix <- paste0(col_prefix, ".")
  raw_main_results %>% 
    select(one_of(meta_cols), starts_with(col_prefix)) %>% 
    gather(starts_with(col_prefix), key = "key", value = !!value)
}

