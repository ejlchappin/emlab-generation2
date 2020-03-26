server <- function(input, output) {
  
  observeEvent(input$toggle_shared_options, {
    toggle("shared_filter_panel")
  })
  
  unit_factor <- reactive({
    available_units %>%
      filter(prefix == input$unit_prefix) %>%
      pull(factor)
  })
  assign("unit_factor", unit_factor, envir = .GlobalEnv)
  
  all_plots <- names(plots)
  
  # for each plot in plots[] produce plots of the namescheme plot_(data_name)_(average/by_iterations)
  map(all_plots, function(plot_name){
    
    # get average of all iterations plot
    
    if(use_plotly){

    output[[paste("plot", plot_name, sep = "_")]] <- renderPlotly({
      plot <- get_plot_filtered(plot_name, input, input$iteration_average)
      ggplotly(plot)
      
    })
    
    } else {
      output[[paste("plot", plot_name, sep = "_")]] <- renderPlot({
        get_plot_filtered(plot_name, input, input$iteration_average)
      })
    }
    
    output$selected_single_plot <- renderPlot({
      get_plot_filtered(input$single_plot_selected, input, input$iteration_average)
    })
    
    output$selected_single_plot_title <- renderText({
      input$single_plot_selected %>% 
        str_replace_all("_", " ") %>% 
        str_to_title()
    })
    
    # # get by iterations plot
    # output[[paste("plot", plot_name, "by_iterations", sep = "_")]] <- renderPlot({
    #   get_plot_filtered(plot_name, input, average = FALSE)
    # })
  })
  
  ## Log files and value boxes
  
  if(analyse_log){
    log_table <- DT::datatable(emlab_log,  filter = list(position = 'top', clear = FALSE))
  } else {
    log_table <- tibble(info = "Logs analysis not activated in config.R.")
 }
  
  output$dt_log_table = DT::renderDataTable({
    log_table
  })
  
  
}