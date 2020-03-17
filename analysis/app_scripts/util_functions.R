
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

#' Sets fixed colors for items such as technologies, fuels...
#' If a named color vector with color is set in conig.R this one is used
#' If not, the colors are assigned using the color palette set
#' 
#'
#' @param items Vector containing names of items
#' @param named_colors_vector_name  Name of variable in config.R
#' @param palette_name  Name of variable in config.R
#'
#' @return named vector with color codes
set_colors <- function(items, named_colors_vector_name, palette_name){
  
  if(exists(named_colors_vector_name)){
    return(get(named_colors_vector_name))  
  } 
  else if(exists(palette_name)){
    colors <- brewer.pal(
      n = length(items), 
      name = get(palette_name)
    )
    names(colors) <- items
    return(colors)
  
  } else {
    stop(glue("{named_colors_vector_name} and {palette_name} not set in config.R"))
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

#' colors for fill. Takes either technology_colors or leaves standard
#'
#' @return
#' @export
#'
#' @examples
scale_color_custom <- function(manual_color_variable){
  
  if(exists(manual_color_variable)){
    scale <- scale_color_manual(values = get(manual_color_variable))
  } else {
    scale <- scale_color_brewer(type = "qual", palette = "Set3")
  }
  
  return(scale)
}



geom_area_shaded <- function(){
  geom_area(colour = "black", size = 0.2, alpha = 0.6)
}

get_plot <- function(data_name, y_label, input, average = TRUE){
  
  #Shared filter
  # TODO simplify iteration stuff.
  my_shared_filters <- get_shared_filters(input)
  
  # Units
  unit_prefix <- input$unit
  unit_factor <- available_units %>% 
    filter(prefix == unit_prefix) %>% 
    pull(factor)
  
  my_labs <- c(
    "average" = "Average over all selected iterations",
    "iterations" = "Selected iterations",
    "x_label" = "Tick (year)",
    "y_label" = glue(y_label)
  )
  
  if(average){
    plot <- data[[data_name]] %>%
      filter_by_shared_filters(my_shared_filters) %>% 
      plots$average[[data_name]](unit_factor, input) +
      labs(
        subtitle = my_labs["average"],
        x = my_labs["x_label"],
        y = my_labs["y_label"]
      )
  } else {
    plot <- data[[data_name]] %>%
      filter_by_shared_filters(my_shared_filters) %>% 
      plots$iterations[[data_name]](unit_factor, input) +
      labs(
        subtitle = my_labs["iterations"],
        x = my_labs["x_label"],
        y = my_labs["y_label"]
      )
  }
  
  
  
  return(plot)
  
}


# UI ----------------------------------------------------------------------

default_mainPanel <- function(title, data_name){
  
  mainPanel(
    titlePanel(title),
    tabsetPanel(
      tabPanel("Average",
               plotOutput(paste_("plot", data_name, "average"))
      ),
      tabPanel("Iterations",
               plotOutput(paste_("plot", data_name, "by_iterations"))
      )
    )
  )
  
}

ui_more_button <- function(){
  actionButton("toggle_shared_options", "More options")
}

# Data parsing ------------------------------------------------------------

get_data_by_prefix <- function(data, col_prefix, value, suffix = "."){
  
  col_prefix <- paste0(col_prefix, suffix)
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



# Data Wrangling ----------------------------------------------------------


#' Filters the data by the global filters iterations and ticks
#'
#' @param data 
#'
#' @return
#' @export
#'
#' @examples
filter_by_shared_filters <- function(data, shared_filters){
  data %>% 
    filter(
      iteration %in% shared_filters$my_iterations
    )
}

# TODO, probably no function needed
get_shared_filters <- function(input){
  list(
    my_iterations = seq(from = input$iterations[1], to = input$iterations[2], by = 1)
    #my_ticks
  )
}

paste_ <- function(...){
  paste(..., sep = "_")
}

