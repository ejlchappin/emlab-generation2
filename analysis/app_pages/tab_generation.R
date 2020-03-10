sidebarLayout(
  
  sidebarPanel(
    
    # Selection of technologies in sidebar
    checkboxGroupInput("technologies_checked", label = h3("Technologies"), 
                       choices = all_technologies,
                       selected = all_technologies)
    
  ), # end sidebarPanel()
  
  # Draw all the plots generated in the Server logic
  mainPanel(
    titlePanel("Generation"),
    plotOutput("plot_generation_average"),
    plotOutput("plot_generation_by_iterations")
    
  )
)
