#
# This is a Shiny web application of EMLab Generation 2. 
# You can run the application by clicking the 'Run App' button above.
#

# This version need to be executed by selecting all and run:
# CMD+A and CMD+Enter

library(shiny)

# Init --------------------------------------------------------------------

# In the init.R all results are read and common variables are prepared
source(file = "app_scripts/init.R")

# theme for ggplot
theme_set(
  theme_bw(base_size = 13) + 
    theme(
      legend.title=element_blank(),
      panel.grid.major = element_blank(), 
      panel.grid.minor = element_blank(),
      legend.spacing.x = unit(0.1, 'cm')
    )
)


# Define functions --------------------------------------------------------

# All plots are defined in this script
source(file = "app_scripts/main.R")

# variables for inputs based on results
all_technologies <- get_sinlge_variable(data$operational_capacities_by_tech, technology)
all_producers <- get_sinlge_variable(data$cash_by_agents, producer)

# Other
technology_colors <- set_technology_colors(all_technologies)

# App UI ------------------------------------------------------------------

# TODO How to draw same element?

iteration_input <- function(input){
  seq(from = input$iterations[1], to = input$iterations[2], by = 1)
}

ui <- navbarPage("EMLab2",
  # defined Menupages here.
  
  navbarMenu(
    "Energy",
    tabPanel("Capacity", source("app_pages/tab_capacity.R")),
    tabPanel("Pipeline capacity", source("app_pages/tab_pipeline_capacity.R")),
    tabPanel("Generation", source("app_pages/tab_generation.R"))
  ),
  header = (
    # Slider for iterations in sidebar
    sliderInput(
      "iterations", 
      label = h3("Iteration Range"), 
      min = iteration_min, max = iteration_max, 
      value = c(iteration_min, iteration_max))
    
  )

)

# App Server ---------------------------------------------------------------



server <- function(input, output) {
  
  # Plot logic is defined here and stored in ouput. 
  # This is a functio, so no , needed
  
  # Define capacity plots
  output$plot_operational_capacities_average <- renderPlot({
    plot_operational_capacities_average(input$technologies_checked, iteration_input(input))
    })
  
  output$plot_operational_capacities_by_iterations <- renderPlot({
    plot_operational_capacities_by_iterations(input$technologies_checked, iteration_input(input))
    })
  
  output$plot_generation_average <- renderPlot({
    plot_generation_average(input$technologies_checked, iteration_input(input))
    })
  
  output$plot_generation_by_iterations <- renderPlot({
    plot_generation_by_iterations(input$technologies_checked, iteration_input(input))
    })
  
  output$plot_pipeline_capacity_average <- renderPlot({
    plot_pipeline_capacity_average(iteration_input(input))
  })
  
  output$plot_pipeline_capacity_by_iterations <- renderPlot({
    plot_pipeline_capacity_by_iterations(iteration_input(input))
  })

  
}

# Run the application 
shinyApp(ui = ui, server = server)

