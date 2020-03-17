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


# Load dynamic plots and pages  --------------------------------------------------------


# All plots are defined in this script
source(file = "app_scripts/main.R")

# App UI ------------------------------------------------------------------

ui <- do.call(navbarPage, c(
  title = if_else(exists("app_title"), app_title, "EMLab2"), 
  app_menu,
  # add shared options that are hidden by default
  header = list(
      list(
        useShinyjs(), 
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
  
  # for each element in app_plots produce plots of the namescheme plot_(data_name)_(average/by_iterations)
  map(app_plots, function(app_plot){
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


