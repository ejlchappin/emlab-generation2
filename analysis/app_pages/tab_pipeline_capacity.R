sidebarLayout(
  
  sidebarPanel(
    
    hr()

  ), # end sidebarPanel()
  
  # Draw all the plots generated in the Server logic
  mainPanel(
    titlePanel("Pipeline capacity"),
    tabsetPanel(
      tabPanel("Average",
        plotOutput("plot_pipeline_capacity_average")
        
      ),
      tabPanel("Iterations",
        plotOutput("plot_pipeline_capacity_by_iterations")
        )
    )
  )
)
