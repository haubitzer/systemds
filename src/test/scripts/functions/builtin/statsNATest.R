args <- commandArgs(TRUE)

library("Matrix")
library("imputeTS")

input_matrix = as.matrix(readMM(paste(args[1], "A.mtx", sep="")))
input_matrix[input_matrix==0] = NA

O = statsNA(input_matrix, bins = 4, print_only = FALSE)


O1=O["length_series"][[1]]
O2=O["number_NAs"][[1]]
O3=O["number_na_gaps"][[1]]
O4=O["average_size_na_gaps"][[1]]
O5=as.numeric(sub("%","",O["percentage_NAs"][[1]],fixed=TRUE))/100
O6=O["longest_na_gap"][[1]]
O7=O["most_frequent_na_gap"][[1]]
O8=O["most_weighty_na_gap"][[1]]

write(O1, paste(args[3], "O1", sep=""))
write(O2, paste(args[3], "O2", sep=""))
write(O3, paste(args[3], "O3", sep=""))
write(O4, paste(args[3], "O4", sep=""))
write(O5, paste(args[3], "O5", sep=""))
write(O6, paste(args[3], "O6", sep=""))
write(O7, paste(args[3], "O7", sep=""))
write(O8, paste(args[3], "O8", sep=""))

'
print(O1)
print(O2)
print(O3)
print(O4)
print(O5)
print(O6)
print(O7)
print(O8)
'