#
# This is a Shiny web application of EMLab Generation 2. 
# You can run the application by clicking the 'Run App' button above.
#

# This version need to be executed by selecting all and run:
# CMD+A and CMD+Enter

#install.packages("shinyjs")

remove(list = ls())

library(shiny)
library(shinyjs)

# Init --------------------------------------------------------------------

# In the init.R all results are read and common variables are prepared
source(file = "app_scripts/init.R")

# theme for ggplot
theme_set(
  theme_bw(base_size = 13) + 
    theme(
      #legend.title=element_blank(),
      legend.spacing.x = unit(0.1, 'cm')
    )
)

# Load plots 
source(file = "app_scripts/main.R")

# App UI ------------------------------------------------------------------

# WIP
# toggle_sidbar_elements <- function(active_element){
# 
#   if(active_element == "technologies"){
#     show("technologies_checked")
#     hide("producers_checked")
#     hide("fuels_checked")
#     hide("all_in_one_plot")
#     hide("flip_tick_segment")
#     hide("segments_checked")
#   } else if (active_element == "producers"){
#     hide("technologies_checked")
#     show("producers_checked")
#     hide("fuels_checked")
#     hide("all_in_one_plot")
#     hide("flip_tick_segment")
#     hide("segments_checked")
#   }  else if (active_element == "fuels"){
#     hide("technologies_checked")
#     hide("producers_checked")
#     show("fuels_checked")
#     hide("all_in_one_plot")
#     hide("flip_tick_segment")
#     hide("segments_checked")
#   }  else if (active_element == "segments"){
#     hide("technologies_checked")
#     hide("producers_checked")
#     hide("fuels_checked")
#     show("all_in_one_plot")
#     show("flip_tick_segment")
#     show("segments_checked")
#   } else {
#     hide("technologies_checked")
#     hide("producers_checked")
#     hide("fuels_checked")
#     hide("all_in_one_plot")
#     hide("flip_tick_segment")
#     hide("segments_checked")
#   }
# }

ui <- fluidPage(
  
 
  sidebarLayout(
  
    sidebarPanel(
      # Selection of technologies in sidebar
      sliderInput(
        "iterations",
        label = h3("Iteration Range"),
        min = iteration_min, max = iteration_max,
        value = c(iteration_min, iteration_max)),
      radioButtons("unit_prefix", "Unit:",all_unit_prefixes),
      hr(),
  
      checkboxGroupInput("technologies_checked", label = h3("Technologies"), 
                         choices = all_technologies,
                         selected = all_technologies),
      hr(),
      checkboxGroupInput("producers_checked", label = h3("Producers"), 
                         choices = all_producers,
                         selected = all_producers),
      hr(),
      checkboxGroupInput("fuels_checked", label = h3("Fuels"), 
                         choices = all_fuels,
                         selected = all_fuels),
      hr(),
      # Segments
      checkboxInput(
        inputId = "all_in_one_plot",
        label = "Segments in one plot",
        value = TRUE),
      checkboxInput(
        inputId = "flip_tick_segment",
        label = "Flip tick and segment",
        value = TRUE),
      
      checkboxGroupInput("segments_checked", label = h3("Segments"), 
                         choices = all_segments,
                         selected = all_segments)
      
    ), 
  mainPanel(
    navbarPage(
      title = if_else(exists("app_title"), app_title, "EMLab2"), 
      navbarMenu(
        "Energy",
        tabPanel("Capacity", default_mainPanel("Operational capacity", "operational_capacities_by_tech")),
        tabPanel("Pipeline capacity", source("app_pages/tab_pipeline_capacity.R")$value),
        tabPanel("Generation", source("app_pages/tab_generation.R")$value),
        tabPanel("Powerplants", source("app_pages/tab_powerplants.R")$value)
      ),
      navbarMenu(
        "Cash",
        tabPanel("Cash", source("app_pages/tab_cash_producers.R")$value),
        tabPanel("Cashflow", default_mainPanel("Cashflows", "cashflows"))
        
      ),
      navbarMenu(
        "Substances",
        tabPanel("CO2 Price", source("app_pages/tab_co2_prices.R")$value),
        tabPanel("CO2 Volumes", source("app_pages/tab_co2_volumes.R")$value),
        tabPanel("Fuel Price", source("app_pages/tab_fuel_prices.R")$value),
        tabPanel("Fuel Volume", source("app_pages/tab_fuel_volumes.R")$value)
        
      ),
        navbarMenu(
           "Spotmarket",
           tabPanel("Average prices",default_mainPanel("Average Electricity prices","average_prices")),
           tabPanel("Market volumes",default_mainPanel("Market volumes","average_volumes")),
           tabPanel("Segment prices", source("app_pages/tab_spot_prices.R")$value),
           tabPanel("Volume", source("app_pages/tab_segment_volume.R")$value),
           tabPanel("Load", source("app_pages/tab_segment_load.R")$value),
           tabPanel("Segment hours", default_mainPanel("Hours", "segment_hours"))
           
    )
  )
)))
  
  
    
 

# App Server ---------------------------------------------------------------

server <- function(input, output) {
  
  observeEvent(input$toggle_shared_options, {
    toggle("shared_filter_panel")
  })
  
  unit_factor <- reactive({ 
    available_units %>%
      filter(prefix == input$unit_prefix) %>%
      pull(factor)
    })
  assign("unit_factor", unit_factor, envir = .GlobalEnv)
  
  all_plots <- names(plots)
  
  # for each plot in plots[] produce plots of the namescheme plot_(data_name)_(average/by_iterations)
  map(all_plots, function(plot_name){
    
    # get average of all iterations plot
    output[[paste("plot", plot_name, "average", sep = "_")]] <- renderPlot({
      get_plot_filtered(plot_name, input)
    })
    
    # get by iterations plot
    output[[paste("plot", plot_name, "by_iterations", sep = "_")]] <- renderPlot({
      get_plot_filtered(plot_name, input, average = FALSE)
    })
  })

}

# Run the application 
shinyApp(ui = ui, server = server)


