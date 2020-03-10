
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

#' Sets colors for plots
#'
#' @param technologies Vector containing technology vectors
#'
#' @return
#' @export
#'
#' @examples
set_technology_colors <- function(technologies){
  
  if(exists("technology_color_palette")){
    technology_colors <- brewer.pal(
      n = length(technologies), 
      name = technology_color_palette
    )
    names(technology_colors) <- technologies
    
  }
  if(exists("technology_colors")){
    return(technology_colors)  
  } else {
    stop("Technology Colors or palette not set in config.R")
  }
  
}

#' colors for fill. Takes either technology_colors or leaves standard
#'
#' @return
#' @export
#'
#' @examples
scale_fill_technologies <- function(){
  
  if(exists("technology_colors")){
    scale <- scale_fill_manual(values = technology_colors)
  } else {
    scale <- scale_fill_brewer(type = "qual", palette = "Set3")
  }
  
  return(scale)
}

geom_area_shaded <- function(){
  geom_area(colour = "black", size = 0.2, alpha = 0.6)
}


# Data parsing ------------------------------------------------------------

get_data_by_prefix <- function(data, col_prefix, value){
  
  col_prefix <- paste0(col_prefix, ".")
  raw_main_results %>% 
    select(one_of(meta_cols), starts_with(col_prefix)) %>% 
    gather(starts_with(col_prefix), key = "key", value = !!value)
}

get_sinlge_variable <- function(data, variable){
  
  variable <- enquo(variable)
  
  data %>% 
    select(!!variable) %>% 
    distinct() %>% 
    pull()
  
}


