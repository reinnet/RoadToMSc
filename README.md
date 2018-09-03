# RoadToMSc
**The Road So Far**

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/d0f55fe2f7dd43cebff8b96737ce56e1)](https://www.codacy.com/app/1995parham/RoadToMSc?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=1995parham/RoadToMSc&amp;utm_campaign=Badge_Grade)

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
## Step by Step from Proposal
When you enter MSc of the Amirkabir University of Technology you must first write a proposal
for your problem and ideas that you have to solve it and present it in professors community.
for this steps see [presentation](presentation/main.pdf) and [proposal](proposal/AUTthesis.pdf).
After the proposal, you are free to do whatever you want with your project until next semester. In the next
semester you must present your idea again for new students and after that, you pass the seminar
course :tada:. At the end, you must complete your thesis and solution to your proposed problem.
I wish you a nice thesis :muscle:.

## Results
### 1
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

### 2
- Node 0: CPU: 1 RAM: 1
- Node 1: CPU: 1 RAM: 1
- Node 2: CPU: 2 RAM: 4
- Node 3: CPU: 1 RAM: 1
- Node 4: CPU: 1 RAM: 1
- Link 0 - 1: BW: 10
- Link 0 - 2: BW: 10
- Link 2 - 0: BW: 10
- Link 1 - 2: BW: 10
- Link 3 - 4: BW: 10
***
- VNFM Required CPU: 1
- VNFM Required RAM: 1
- VNFM Required BW: 1
***
- Type 0: CPU: 1 RAM: 1
***
* Chain 0:
  - Node 0: Type 0
* Chain 1:
  - Node 0: Type 0
  - Node 1: Type 0
  - Node 2: Type 0
  - Link 0 - 1: BW: 1
  - Link 0 - 2: BW: 1

```
Found incumbent of value 0.000000 after 0.00 sec. (0.01 ticks)
Tried aggregator 2 times.
MIP Presolve eliminated 47 rows and 9 columns.
MIP Presolve added 7 rows and 7 columns.
Aggregator did 17 substitutions.
Reduced MIP has 38 rows, 53 columns, and 160 nonzeros.
Reduced MIP has 46 binaries, 7 generals, 0 SOSs, and 0 indicators.
Presolve time = 0.00 sec. (0.26 ticks)
Probing time = 0.00 sec. (0.04 ticks)
Tried aggregator 1 time.
MIP Presolve eliminated 7 rows and 7 columns.
MIP Presolve added 7 rows and 7 columns.
Reduced MIP has 38 rows, 53 columns, and 160 nonzeros.
Reduced MIP has 46 binaries, 7 generals, 0 SOSs, and 0 indicators.
Presolve time = 0.00 sec. (0.12 ticks)
Probing time = 0.00 sec. (0.04 ticks)
Clique table members: 54.
MIP emphasis: balance optimality and feasibility.
MIP search method: dynamic search.
Parallel mode: deterministic, using up to 8 threads.
Root relaxation solution time = 0.00 sec. (0.09 ticks)

        Nodes                                         Cuts/
   Node  Left     Objective  IInf  Best Integer    Best Bound    ItCnt     Gap

*     0+    0                            0.0000       20.0000              --- 
*     0+    0                           10.0000       20.0000           100.00%
*     0     0      integral     0       20.0000       20.0000       15    0.00%
Elapsed time = 0.00 sec. (0.68 ticks, tree = 0.00 MB, solutions = 3)

Root node processing (before b&c):
  Real time             =    0.01 sec. (0.68 ticks)
Parallel b&c, 8 threads:
  Real time             =    0.00 sec. (0.00 ticks)
  Sync time (average)   =    0.00 sec.
  Wait time (average)   =    0.00 sec.
                          ------------
Total (root+branch&cut) =    0.01 sec. (0.68 ticks)

 Solution Status = Optimal

 cost = 20.0

 >> Chains
Chain 0 is accepted.
Chain 1 is accepted.


 >> Instance mapping
Chain 0:
Node 0 with type 0 is mapped on 3
Chain 1:
Node 0 with type 0 is mapped on 2
Node 1 with type 0 is mapped on 1
Node 2 with type 0 is mapped on 2


 >> Manager mapping
Chain 0 manager is 4
Chain 1 manager is 0


 >> Instance links
Chain 1 link 1 (0 - 2) is on 2-0
Chain 1 link 0 (0 - 1) is on 3-4


 >> Management links
Chain 0 node 0 manager is on 3-4
Chain 1 node 1 manager is on 1-2
Chain 1 node 0 manager is on 2-0
Chain 1 node 1 manager is on 2-0
Chain 1 node 2 manager is on 2-0
```

## Presented in

| Location | Date |
|:-------- |:----:|
| Group Meeting | 2017-12-19 |
| Dr.Bakhshi Meeting | 2018-01-23 |
| Dr.Bakhshi Meeting | 2018-04-24 |
| Network Community Meeting | 2018-04-25 |
