# RoadToMSc
**The Road So Far**

## Intorduction
I am MSc student of the Amirkabir University of Technology.
My research interest is NFV and my thesis about placing and routing SFC requests with management constraints.
By management constraints, we mean providing VNFM for each SFC.
Problem formulation implemented in [IBM Cplex](https://www.ibm.com/analytics/cplex-optimizer) and can be accessed in the
`simulation` folder.  For running simulation, you need Cplex installed on your system after you have Cplex installed you can
run the simulation with following java option:

```
"-Djava.library.path=$USER_HOME$/Application/CPLEX_Studio-Community128/cplex/bin/x86-64_linux"
```
## Results

- Node 0: CPU: 1 RAM: 1
- Node 1: CPU: 1 RAM: 1
- Link 0 - 1: BW: 10
***
- VNFM Required CPU: 1
- VNFM Required RAM: 1
- VNFM Required BW: 1
***
- Type 0: CPU: 1 RAM: 1
***
* Chain 0:
  - Node 0: Type 0

```
Found incumbent of value 0.000000 after 0.00 sec. (0.00 ticks)
Tried aggregator 2 times.
MIP Presolve eliminated 12 rows and 4 columns.
Aggregator did 6 substitutions.
All rows and columns eliminated.
Presolve time = 0.00 sec. (0.02 ticks)

Root node processing (before b&c):
  Real time             =    0.00 sec. (0.02 ticks)
Parallel b&c, 8 threads:
  Real time             =    0.00 sec. (0.00 ticks)
  Sync time (average)   =    0.00 sec.
  Wait time (average)   =    0.00 sec.
                          ------------
Total (root+branch&cut) =    0.00 sec. (0.02 ticks)

 Solution Status = Optimal

 cost = 10.0

 >> Chains
Chain 0 is accepted.


 >> Instance mapping
Chain 0:
Node 0 with type 0 is mapped on 0


 >> Manager mapping
Chain 0 manager is 1


 >> Instance links


 >> Management links
Chain 0 node 0 manager is on 0-1
```

## Presented in

| Location | Date |
|:-------- |:----:|
| Group Meeting | 2017-12-19 |
| Dr.Bakhshi Meeting | 2018-01-23 |
| Dr.Bakhshi Meeting | 2018-04-24 |
| Network Community Meeting | 2018-04-25 |
