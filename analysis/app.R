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
      legend.title=element_blank(),
      legend.spacing.x = unit(0.1, 'cm')
    )
)

# Load plots 
source(file = "app_scripts/main.R")

# App UI ------------------------------------------------------------------

# This is the main menu a adds the plot pages to the app
app_menu <- list(
  navbarMenu(
    "Energy",
    tabPanel("Powerplants", source("app_pages/tab_powerplants.R")$value),
    tabPanel("Capacity", source("app_pages/tab_capacity.R")$value),
    tabPanel("Pipeline capacity", source("app_pages/tab_pipeline_capacity.R")$value),
    tabPanel("Generation", source("app_pages/tab_generation.R")$value)
  ),
  navbarMenu(
    "Cash",
    tabPanel("Cash", source("app_pages/tab_cash_producers.R")$value)
  ),
  navbarMenu(
    "Substances",
    tabPanel("CO2 Price", source("app_pages/tab_co2_prices.R")$value),
    tabPanel("CO2 Volumes", source("app_pages/tab_co2_volumes.R")$value),
    tabPanel("Fuel Price", source("app_pages/tab_fuel_prices.R")$value),
    tabPanel("Fuel Volume", source("app_pages/tab_fuel_volumes.R")$value)
  )#,
  # navbarMenu(
  #   "Spotmarket",
  #   tabPanel("Average price", source("app_pages/tab_spot_averge.R")$value),
  #   tabPanel("Total volume", source("app_pages/tab_spot_total_volume.R")$value),
  #   tabPanel("Price", source("app_pages/tab_spot_price.R")$value),
  #   tabPanel("Volume", source("app_pages/tab_spot_volume.R")$value)
  # )
)

# Everything else
ui <- do.call(navbarPage, c(
  title = if_else(exists("app_title"), app_title, "EMLab2"), 
  app_menu,
  # add shared options that are hidden by default
  header = list(
      list(
        useShinyjs(), 
        ui_more_button(),
        hidden(
          wellPanel(id = "shared_filter_panel",
            fluidRow(
              column(
                6,
                sliderInput(
                  "iterations",
                  label = h3("Iteration Range"),
                  min = iteration_min, max = iteration_max,
                  value = c(iteration_min, iteration_max))
                ),

              column(
                6, radioButtons("unit", "Unit:",all_my_units))
            )

          )
        )
      )
    )
  )
)

# App Server ---------------------------------------------------------------

server <- function(input, output) {
  
  observeEvent(input$toggle_shared_options, {
    toggle("shared_filter_panel")
  })
  
  # for each plot in plots[] produce plots of the namescheme plot_(data_name)_(average/by_iterations)
  map(plots$meta, function(app_plot){
    
    
    # get average of all iterations plot
    output[[paste("plot", app_plot$data_name, "average", sep = "_")]] <- renderPlot({
      get_plot(app_plot$data_name, app_plot$y_label, input)
    })
    
    # get by iterations plot
    output[[paste("plot", app_plot$data_name, "by_iterations", sep = "_")]] <- renderPlot({
      get_plot(app_plot$data_name, app_plot$y_label, input, FALSE)
    })
  })

}

# Run the application 
shinyApp(ui = ui, server = server)


