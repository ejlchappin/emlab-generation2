sidebarLayout(
  
  sidebarPanel(
    # Selection of technologies in sidebar
    checkboxGroupInput("fuels_checked_vol", label = h3("Fuels"), 
                       choices = all_fuels,
                       selected = all_fuels)
    
  ), # end sidebarPanel()
  
  default_mainPanel("Fuel volumes", "fuel_volumes")

)
