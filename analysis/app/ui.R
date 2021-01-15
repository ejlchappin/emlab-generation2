library(shinydashboard)
source("ui_filters.R")

ui <- dashboardPage(
  dashboardHeader(
    title = if_else(exists_config_param("app_title"), config_params[["app_title"]], "EMLab2")
  ),
  dashboardSidebar(
    sidebarMenu(
      
     
      menuItem(
        "Single plots", tabName = "single_plots", icon = icon("lightbulb")
      ),
      menuItem(
        "Logs", tabName = "logs", icon = icon("file-alt")),
      
      radioButtons("unit_prefix", "Global unit:",all_unit_prefixes, inline = TRUE)

    )
  ),
  dashboardBody(
    
    tabItems(
      tabItem(tabName = "dashboard",
              h2("Dashboard tab content"),
              p("The plan is to enable user to customise this dashboard with content"),
              fluidRow(
                box(title = "Box title", "Box content"),
                box(status = "warning", "Box content")
              )
      ),
      tabItem(tabName = "logs",
              h2("Logfile"),
              DT::dataTableOutput("dt_log_table")
      ),
      
      tabItem(tabName = "single_plots",
              
              fluidRow(
                box(
                  title = textOutput("scenario_descriptions_title"), 
                  width = 12, solidHeader = TRUE, collapsible = TRUE, collapsed = TRUE, status ="info",
                  column(
                    width = 6, 
                    textInput("file_scenario_name", label = "Custom name", value = scenario_descriptions_initial_name),
                    br(),
                    p(paste("Filename:", prefix, "(...)"))
                  ),
                  
                  column(
                    width = 6,
                    textAreaInput(
                      inputId = "file_scenario_caption", 
                      label = "Description", 
                      value = scenario_descriptions_initial_caption,
                      rows = 3,
                      resize = "vertical"),
                    actionButton("submit", "Submit")
                    )
                    
                )
                       
              ),

              fluidRow(
                column(width = 9,
                       box(title = textOutput("selected_single_plot_title"), status = "primary", width = 12, height = "100%", solidHeader = TRUE,
                           plotOutput("selected_single_plot", height = "100%")
                           )
                ),
                ui_filtersidebar()
            )
              
      ),
      
      tabItem(tabName = "data",
              h2("Data"),
              box(title = "Box title", status = "warning", "TODO: Show data")
      
      )
    )
  )
)