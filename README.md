# query2optimization

- Download Gurobi and access license
[Look at here](https://www.gurobi.com/downloads/?campaignid=2027425882&adgroupid=77414946611&creative=355014679679&keyword=gurobi&matchtype=e&gclid=CjwKCAiAt9z-BRBCEiwA_bWv-GqDPUVSJiJxVNYOdZKrl0sbrTZchoi1jTlXNEMlwihDBg4sCIEkZxoCI-4QAvD_BwE)

- Import Gurobe jar file as external library in InteliJ IDEA

## Sample Queries

- Image

```
(object=car and color=red) or (object=truck and color=yellow);
```

- Text

```
(class=toxic and class=severe_toxic) or class=obscene or class=identity_hate;
```

- (Video)

```
object=car and color=red and position=left;
```

## Parser - JavaCC
- Input: query (string)
```Java
private static Query2Steps tester = new Query2Steps();
String query = "(class=toxic and class=severe_toxic) or class=obscene or class=identity_hate;";
String output = tester.parser(query);
```

- Output: steps (line of strings)
```
car&red,S1
truck&yellow,S2
S1||S2,S3
```

## Optimizer - Gurobi
- Input: output steps, properties(Map)
```Java
public static  String Tasks[] =
      new String[] { "car", "truck", "red", "yellow";

public static  String Models[] =
      new String[] { "model1", "model2","model3","model4","model5","model6"};

public static double accuracy[][] = new double[][]{
      {0.96,0.98,0,0,0,0},
      {0,0,0.93,0.95,0,0},
      {0,0,0,0,0.96,0.98},
      {0,0,0,0,0.96,0.98}};

public static double cost[][] =
      new double[][] {
          {5,10,300,300,300,300},
          {300,300,20,40,300,300},
          {300,300, 300,300, 5,10},
          {300,300,300,300,20,40}};
```
- Output: model names
```
Thread count: 4 physical cores, 8 logical processors, using up to 8 threads
Optimize a model with 5 rows, 33 columns and 36 nonzeros
Model fingerprint: 0x58c175e3
Model has 7 quadratic constraints
Coefficient statistics:
  Matrix range     [1e+00, 1e+00]
  QMatrix range    [1e+00, 1e+00]
  QLMatrix range   [9e-01, 1e+00]
  Objective range  [1e+00, 1e+00]
  Bounds range     [1e+00, 1e+00]
  RHS range        [1e+00, 2e+02]
Presolve removed 1 rows and 18 columns

Continuous model is non-convex -- solving as a MIP.

Presolve removed 5 rows and 26 columns
Presolve time: 0.00s
Presolved: 12 rows, 7 columns, 28 nonzeros
Presolved model has 3 bilinear constraint(s)
Variable types: 7 continuous, 0 integer (0 binary)

Root relaxation: objective 9.990000e-01, 7 iterations, 0.00 seconds

    Nodes    |    Current Node    |     Objective Bounds      |     Work
 Expl Unexpl |  Obj  Depth IntInf | Incumbent    BestBd   Gap | It/Node Time

*    0     0               0       0.9990000    0.99900  0.00%     -    0s

Explored 0 nodes (7 simplex iterations) in 0.01 seconds
Thread count was 8 (of 8 available processors)

Solution count 1: 0.999 
No other solutions better than 0.999

Optimal solution found (tolerance 1.00e-04)
Best objective 9.990000000000e-01, best bound 9.990000000000e-01, gap 0.0000%
The optimal objective is 0.999
car.model2
1.0000000000000002
truck.model4
1.0
red.model6
1.0000000000000002
yellow.model6
0.9693877551020409
```
