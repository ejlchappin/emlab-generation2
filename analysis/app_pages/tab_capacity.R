sidebarLayout(
  
  sidebarPanel(
    
    # Selection of technologies in sidebar
    checkboxGroupInput("technologies_checked", label = h3("Technologies"), 
                       choices = all_technologies,
                       selected = all_technologies)

  ), # end sidebarPanel()
  
  # Draw all the plots generated in the Server logic
  mainPanel(
    titlePanel("Capacity"),
    tabsetPanel(
      tabPanel("Average",
        plotOutput("plot_operational_capacities_average")

      ),
      tabPanel("Iterations",
        plotOutput("plot_operational_capacities_by_iterations")
        )
    )
  )
)
