sidebarLayout(
  
  sidebarPanel(
    # Selection of technologies in sidebar
    checkboxGroupInput("producers_checked", label = h3("Producers"), 
                       choices = all_producers,
                       selected = all_producers)
    
  ), # end sidebarPanel()
  
  default_mainPanel("Cash by producers", "cash_by_producers")

)
