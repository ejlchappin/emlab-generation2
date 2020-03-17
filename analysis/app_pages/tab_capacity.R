sidebarLayout(
  
  sidebarPanel(
    # Selection of technologies in sidebar
    checkboxGroupInput("technologies_checked", label = h3("Technologies"), 
                       choices = all_technologies,
                       selected = all_technologies)

  ), # end sidebarPanel()
  
  default_mainPanel("Operational capacity", "operational_capacities_by_tech")
  
)
