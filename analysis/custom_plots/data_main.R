
# In this example, one plot is customized
# Copy from app_plots/ the appropriate plot function that you want to customize into here

# Operational capacity custom plot ----------------------------------------------------

# if(process_data("operational_capacities")){
#   
#   plots[["operational_capacities_by_technology"]] <- function(data, input, average = TRUE){
#     
#     data <- data %>%
#       filter(technology %in% input$technologies_checked)
#     
#     if(average){
#       # Average over all iterations
#       plot <- data %>% 
#         group_by(tick, market, technology) %>% 
#         summarise(avg_capacity = mean(capacity)) %>% 
#         ggplot(mapping = aes(y = avg_capacity * unit_factor())) +
#         facet_grid(~ market)
#     } else {
#       # By Iterations
#       plot <- data %>%
#         ggplot(mapping = aes(y = capacity * unit_factor())) +
#         facet_wrap(iteration ~ market)
#     }
#     plot +
#       geom_area_shaded(mapping = aes(x = tick, fill = technology)) +
#       scale_fill_technologies() + 
#       labs_default(
#         title = "Test plot", # This is different
#         y = glue("Capacity ({input$unit_prefix}W)"),
#         subtitle = default_subtitle(average),
#         fill = "Technology")
#   }
#   
# }


