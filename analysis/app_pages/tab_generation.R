sidebarLayout(
  
  sidebarPanel(
    # Selection of technologies in sidebar
    checkboxGroupInput("technologies_checked_gen", label = h3("Technologies"), 
                       choices = all_technologies,
                       selected = all_technologies)
    
  ), # end sidebarPanel()

  default_mainPanel("Generation", "generation_total")

)
