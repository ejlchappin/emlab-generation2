
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

read_results <- function(filename, custom_col_types = cols(.default = "n")){
  read_delim(
    file = get_filepath(filename),
    delim = ";",
    col_types = custom_col_types,
    locale = locale(decimal_mark = ".", grouping_mark =  "'")) %>% 
    arrange(iteration, tick)
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


labs_default <- function(
  x = "Tick (year)", 
  y = waiver(), 
  title = waiver(), 
  subtitle = waiver(), 
  caption = waiver(),
  tag = waiver(), 
  ...){
  
  labs(
    x = x,
    y = y,
    title = title,
    subtitle = subtitle,
    caption = caption,
    tag = tag,
    ... = ...
  )
}

default_subtitle <- function(average){
  if_else(average, "Average over all selected iterations", "Selected iterations")
}

#' Gets either an average of by iterations plot defined in plots[] and applies common filters
#'
#' @param plot_name name of the plot
#' @param input input
#' @param average return a plot where iterations a averaged or single 
#'
#' @return
#' @export
#'
#' @examples
get_plot_filtered <- function(plot_name, input, average = TRUE){

  if(plot_name %in% names(data)){

    # TODO adjust for more flexibility
    shared_filters <- list(
        my_iterations = seq(from = input$iterations[1], to = input$iterations[2], by = 1)
        #my_ticks
      )
    
    plot <- data[[plot_name]] %>%
      filter(
        iteration %in% shared_filters$my_iterations
        ) %>% 
      plots[[plot_name]](input, average)
    
  } else {
    stop(glue("{plot_name} does not exist in data[] in main.R. Please choose a plot_name defined in plot AND data."))
  }
  
  
  return(plot)
  
}


# UI ----------------------------------------------------------------------

#' Default tab layout showing average and iterations
#'
#' @param title Title of tab
#' @param data_name Data
#' @param ... parameters passed to imageOutput 
#'
#' @return mainPanel()
default_mainPanel <- function(title, data_name, ...){
  
  #mainPanel(
  
  
  tabsetPanel(
    
      tabPanel("Average",
               tags$p(""),
               tags$h3(title),
               plotOutput(paste_("plot", data_name, "average"),...)
      ),
      tabPanel("Iterations",
               tags$p(""),
               tags$h3(title),
               plotOutput(paste_("plot", data_name, "by_iterations"),...)
      )
    #)
  )
  
}

ui_more_button <- function(){
  actionButton("toggle_shared_options", "More options")
}

# Data parsing ------------------------------------------------------------

get_data_by_prefix <- function(data, col_prefix, value, suffix = "."){
  
  col_prefix <- paste0(col_prefix, suffix)
  data %>% 
    select(one_of(meta_cols), starts_with(col_prefix)) %>% 
    gather(starts_with(col_prefix), key = "key", value = !!value)
}


#' Transforms column in raw data of wide format into multiple variables
#' 
#' This first selects all columns in the tibble `data` that begin with `prefix`
#' and then gathers the data in the column names to one or multiple variables in `vars`
#'
#' @param data data
#' @param prefix prefix to look for in columns. All columns that start with this prefix will be considered for the gathering into long format
#' @param vars character vector of the variables to be extraced.
#' @param value name for the value column
#'
#' @return data in long format
#' @export
#'
#' @examples 
#'  data[["test"]] <- raw_marketinformation_results %>% 
#'  get_vars_from_multiple_columns(prefix = "segment", vars = "type", value = "energy")
get_vars_from_multiple_columns <- function(data, prefix, vars, value){
  
  prefix_cols <- paste("prefix_",str_split(prefix, "\\.")[[1]])
  
  data <- data %>% 
    get_data_by_prefix(col_prefix = prefix, value = value) %>% 
    separate(col = "key", into = c(prefix_cols,vars), sep = "\\.") %>% 
    select(-one_of(prefix_cols))
  
  return(data)
  
}

get_sinlge_variable <- function(data, variable){
  
  variable <- enquo(variable)
  
  data %>% 
    select(!!variable) %>% 
    distinct() %>% 
    pull()
  
}



# Data Wrangling ----------------------------------------------------------

paste_ <- function(...){
  paste(..., sep = "_")
}


# Report function ---------------------------------------------------------

## Will be overwritten by reactive function in app.R for apps, so only used by reports
unit_factor <- function(){ 
  available_units %>%
    filter(prefix == global_unit) %>%
    pull(factor)
}



