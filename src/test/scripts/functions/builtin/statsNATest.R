args <- commandArgs(TRUE)

library("Matrix")
library("imputeTS")

input_matrix = as.matrix(readMM(paste(args[1], "A.mtx", sep="")))
input_matrix[input_matrix==0] = NA

B = statsNA(input_matrix, bins = 4, print_only = TRUE)
