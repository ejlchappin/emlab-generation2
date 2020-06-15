
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
get_result_files <- function(directory){
  
  file_list <- list.files(path = directory, pattern = "*.csv")
  
  tibble(
    file = file_list,
    date = file.info(paste0(directory,file_list))$ctime
  ) %>% 
    arrange(date) %>% 
    separate(col = "file", into = c("id", "scenario", "model", "reporter", "datatype"), sep = "-", remove = FALSE)
  
}

#' Get a list of all log files from the results folder
#'
#' @param directory path to results files
#'
#' @return a tibble() of all log files
get_log_files <- function(directory){
  
  file_list <- list.files(path = directory, pattern = "*-log.txt$")
  
  tibble(
    file = file_list,
    date = file.info(paste0(directory,file_list))$ctime
  ) %>% 
    arrange(date) %>% 
    separate(col = "file", into = c("id", "datatype"), sep = "-", remove = FALSE)
}


filter_latest_files <- function(file_df){
  
  lastest_id <- file_df %>% 
    select(id, date) %>% 
    filter(date == max(date)) %>% 
    slice(1) %>% 
    pull(id)
  
  
  file_df %>%
    filter(id == lastest_id)
} 

filter_selected_files <- function(file_df, selected_id){
  file_df %>%
    filter(id == selected_id)
} 


#' Reads a specific file of the current scenario and id. 
#'
#' @param file_list list containting all csvs, with the columns datatype and file
#' @param datafile the name  of the .csv, e.g. "main.csv"
#'
#' @return a filename
#' @export
#'
#' @examples
#' @TODO generalise function
read_emlab_results <- function(file_list, datatype, custom_col_types = cols(.default = "n")){
  
  if(datatype %in% file_list$datatype){
    filepath <- file_list %>% 
      get_results_filepath(datatype)
    
    read_delim(
      file = filepath,
      delim = ";",
      col_types = custom_col_types,
      locale = locale(decimal_mark = ".", grouping_mark =  "'")) %>% 
      arrange(iteration, tick)
    
  } else {
    warning(paste(datatype, "does not exist for this prefix"))
  }
  
}

read_emlab_log <- function(file_list){
  
  filepath <- file_list %>% 
    get_results_filepath("log.txt")

  read_lines(file = filepath)
}


get_results_filepath <- function(file_list, datatype, directory = emlab_results_directory){
  file <- file_list %>% 
    filter(datatype == !!datatype) %>% 
    pull(file)
  
  paste0(directory, file)
}


read_emlab_csv_log <- function(file_list){
  
  
  filepath <- file_list %>% 
    get_results_filepath("log.txt")
  
  read_delim(file = filepath, delim = ";") %>% 
    mutate(
      time = as.POSIXct(time/1000, origin="1970-01-01"),
      class = str_sub(class, 10) # remove emlab.gen. package name
      )
  
}


# Files: description ------------------------------------------------------


#outputDir <- "test"



# treat whole dataset, 


save_to_description_file <- function(file, prefix, name, caption) {

  if(prefix %in% scenario_descriptions$prefix){
    
    new_scenario_descriptions <-scenario_descriptions %>% 
      mutate(
        file_scenario_name = ifelse(prefix == !!prefix, name, file_scenario_name),
        file_scenario_caption = ifelse(prefix == !!prefix, caption, file_scenario_caption)
      )
    } else {
      new_scenario_descriptions <-scenario_descriptions %>% 
        add_row(
          prefix = !!prefix,
          file_scenario_name = name,
          file_scenario_caption = caption
        )
    }
    
  write_csv(
    x = new_scenario_descriptions,
    path = file)
}

load_description_file <- function(file) {
  
  if(file.exists(file)){
    data <- read_csv(file)
  }
  else {
    data <- tibble(
      prefix = character(0),
      file_scenario_name = character(0), 
      file_scenario_caption = character(0))
  }
  
  data
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
    
    if(length(colors) < length(items)){
      stop(glue("There are not enough colors in the brewer palette {get(palette_name)} for your scenario. 
                Please assign other palette to {palette_name} or assign own colors to {named_colors_vector_name} in config.R"))
    } else {
      names(colors) <- items
      return(colors)
    }

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


#' colors for fill. Takes either technology_colors or leaves standard
#'
#' @return
#' @export
#'
#' @examples
scale_fill_custom <- function(manual_color_variable){
  
  if(exists(manual_color_variable)){
    scale <- scale_fill_manual(values = get(manual_color_variable))
  } else {
    scale <- scale_fill_brewer(type = "qual", palette = "Set3")
  }
  
  return(scale)
}



geom_area_shaded <- function(...){
  geom_area(..., colour = "black", size = 0.2, alpha = 0.6)
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

variable_name_to_title <- function(titles){
  titles %>% 
    str_replace_all("_", " ") %>% 
    str_to_sentence()
  
}

get_title_of_selected_plot <- function(input){
  input$single_plot_selected %>% variable_name_to_title()  
}


single_plot_select_names <- function(names){
  
  variable <- names
  names(variable) <- variable_name_to_title(names)
  variable
  
}

# Data parsing ------------------------------------------------------------

get_data_by_prefix <- function(data, col_prefix, value, suffix = "."){
  
  if(col_prefix != ""){
    col_prefix <- paste0(col_prefix, suffix)
    data %>% 
      select(one_of(meta_cols), starts_with(col_prefix)) %>% 
      gather(starts_with(col_prefix), key = "key", value = !!value)
  } else {
    warning("prefix for column must be given")
  }

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

#' Gets one column and the meta_cols from a reporter file. 
#'
#' @param data reporter data
#' @param prefix Either a prefix or NULL if no prefix give
#' @param value name of the variable (part after the prefix) as character
#'
#' @return
#' @export
#'
#' @examples
get_var_from_single_column <- function(data, prefix, value){
  
  if(!is.null(prefix)){
    data <- data %>% 
      get_data_by_prefix(col_prefix = prefix, value = value, suffix = "") %>% 
      select(-key)
  } else {
    
    data <- data %>% 
      select(one_of(c(meta_cols, value)))
  }
  
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



