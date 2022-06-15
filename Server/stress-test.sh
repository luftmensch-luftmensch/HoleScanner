#!/usr/bin/env bash

for N in {1..1000}
do
  ./client &
done
wait
