# -----------------------------------------------------------------------------------
# EMLab package example code with instructions
# -----------------------------------------------------------------------------------

# Install package dependencies
install.packages(c("sqldf", "dplyr", "ggplot2", "RColorBrewer"), dependencies = TRUE)

# Install the 'emlab' package going to Tools > Install Packages.. > Install from: Package Archive File 
# and select a locally stored version, e.g. 'emlab_0.1.0.tar.gz'

# After installing the package's dependencies and installing the emlab package, load the emlab library with
library(emlab)

# Define your directory location (where data files are located and plots will be stored). Make sure you use '/', not '\'
directory = "C:/Users/USERNAME/Documents/EMLab/emlab2-analysis"
# Define the file name from which to read. Make sure it is in your directory folder and has '.csv' at the end.
filename = "0000000000000-Scenario_XX-EMlabModelRole-DefaultReporter-main.csv"
# Read your main.csv data into a cleaned dataframe using
data = ReadMainData(filename, directory)

# Create a plot, for example operational generation capacity (for info about a function, hover over PlotCapacity and hit F1)
PlotCapacity(data, detail=FALSE)
# This will save the plot(s) to your directory. If you want to modify the graphs themselves, store the output in a variable:
CapacityPlots = PlotCapacity(data, detail=FALSE)
# You can then access the plots individually with CapacityPlots[[1]], CapacityPlots[[2]], etc.
CapacityPlots[[1]]

# Another example function
PlotNrPowerPlants(data, return=TRUE)
