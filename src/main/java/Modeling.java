import gurobi.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Modeling {
  Map<String,Object> dict ;
  String[] Tasks  =Config.Tasks;
  String[] Models =Config.Models;
  double[][] cost;
  double[][] accuracy;
  Double COSTBOUND = Config.COSTBOUND;
  Double ACCURACYBOUND = Config.ACCURACYBOUND;
  int nModels;
  int nTasks;

  GRBEnv env;
  GRBModel model;
  String query;

  Modeling(String id,String query) throws GRBException, FileNotFoundException, URISyntaxException {
    switch(id) {
    case "image":
      // code block
      break;
    case "text":
      URL modelPath = this.getClass().getClassLoader().getResource("model"); // "model"
      dict = Config.getTextStatistics(modelPath);
      break;
    default:
      dict = Config.getSysthesisData();
    }
    Tasks = (String[]) dict.get("Task");
    Models = (String[]) dict.get("Model");
    System.out.println(Arrays.toString(Models));
    nTasks = Tasks.length;//Tasks.length;
    nModels = Models.length;
    cost = (double[][]) dict.get("Cost");
    accuracy = (double[][]) dict.get("Accuracy");
    System.out.println(Arrays.deepToString(accuracy));

    this.query = query;
    env = new GRBEnv();
    model = new GRBModel(env);
    model.set(GRB.StringAttr.ModelName, "assignment");
    model.set(GRB.IntParam.NonConvex, 2);
  }



  Modeling(String query) throws GRBException {
    this.query = query;
    env = new GRBEnv();
    model = new GRBModel(env);
    model.set(GRB.StringAttr.ModelName, "assignment");
    model.set(GRB.IntParam.NonConvex, 2);
  }

  protected void gurobiOptimizer() throws IOException, GRBException {
      // Model
      // Assignment variables: x[w][s] == 1 if worker w is assigned
      // to shift s. Since an assignment model always produces integer
      // solutions, we use continuous variables and solve as an LP.
      GRBVar[][] x = new GRBVar[nTasks][nModels];
      for (int w = 0; w < nTasks; ++w) {
        for (int s = 0; s < nModels; ++s) {
          x[w][s] =
              model.addVar(0, 1, 1, GRB.CONTINUOUS,
//              model.addVar(0, 1, 1, GRB.INTEGER,
                  Tasks[w] + "." + Models[s]);
        }
      }

      // Linear constraint (cost) & Task constraint
      GRBLinExpr costExpr = getCostExpr(x);

      // Quadratic constraint (accuracy)
      GRBQuadExpr accuracyExpr = getAccuracyExpr(this.query,x);

      /*
      Constraint and objective function vary
      occording to the constraint target: accuracy/cost
       */
      if (Config.constraint == "accuracy"){
        model.setObjective(costExpr, GRB.MINIMIZE);
        model.addQConstr(accuracyExpr, GRB.GREATER_EQUAL, ACCURACYBOUND, "Accuracy");
      }else{
        model.addConstr(costExpr, GRB.LESS_EQUAL, COSTBOUND, "Cost");
        model.setObjective(accuracyExpr, GRB.MAXIMIZE);
      }

      // Optimize
      model.optimize();
      showResults(x);

      // Dispose of model and environment
      model.dispose();
      env.dispose();
  }


  public Map<String, GRBQuadExpr> getTaskAccuracyExpr(GRBVar[][] x) throws GRBException {
    Map<String, GRBQuadExpr> taskAccuracyExpression = new HashMap<>();
    for (int t = 0; t < nTasks; t++) {
//      System.out.println(Tasks[t]);
      taskAccuracyExpression.put(Tasks[t], new GRBQuadExpr());
      taskAccuracyExpression.get(Tasks[t]).addTerms(accuracy[t], x[t]);
    }
    return taskAccuracyExpression;
  }

  public GRBLinExpr getCostExpr(GRBVar[][] x) throws GRBException {
    // Constraint: assign exactly one model to each tasks t
    // Constraint: assign cost that match the constraints
    double[] Models_availability = Config.getOnes(nModels);
    GRBLinExpr costExpr = new GRBLinExpr();
    for (int t = 0; t < nTasks; t++) {
//      System.out.println(Tasks[t]);
      GRBLinExpr lhs = new GRBLinExpr();
      // ∑C*x[t][m] < Cost
      costExpr.addTerms(cost[t], x[t]);
      // ∑x[m] == 1
      lhs.addTerms(Models_availability, x[t]);
      // assign exactly one model to each task
      model.addConstr(lhs, GRB.EQUAL, 1.0, Tasks[t] + ".Constraint");
    }
    return costExpr;
  }

  public GRBQuadExpr getAccuracyExpr(String query, GRBVar[][] x) throws GRBException {
    // where expression is stored in GRBVar
    Map<String, GRBVar> stepAccuracyExpression = new HashMap<>();
    // Read Computation Steps
//    BufferedReader br = readFile(path); //"src/main/java/Query2Steps/output.txt"
    String[] steps = query.split("\n");
    String name = "";

    //Read File Line By Line
    for(String strLine:steps){
      System.out.println(strLine);
      // return [name, operator, objects]
      List<Object> expression = getSteps(strLine);
      name = (String) expression.get(0);
      String operator = (String) expression.get(1);
      String[] objects = (String[]) expression.get(2);
      GRBQuadExpr qhs_accu = new GRBQuadExpr();

      // Set quadratic expression for each step, name with the step name
//        stepAccuracyExpression.put(name, new GRBVar);
      // Add equation to variable
      GRBVar v1 = model.addVar(0, 1, 1, GRB.CONTINUOUS, objects[0] + ".express");
      GRBVar v2 = model.addVar(0, 1, 1, GRB.CONTINUOUS, objects[1] + ".express");

      Map<String, GRBQuadExpr> taskAccuracyExpression = getTaskAccuracyExpr(x);
      // v1 = task1.express
      if (stepAccuracyExpression.containsKey(objects[0])) {
      } else {
        model.addQConstr(v1, GRB.EQUAL, taskAccuracyExpression.get(objects[0]), objects[0]);
        stepAccuracyExpression.put(objects[0], v1);
      }
      if (stepAccuracyExpression.containsKey(objects[1])) {
      } else {
        model.addQConstr(v1, GRB.EQUAL, taskAccuracyExpression.get(objects[1]), objects[1]);
        stepAccuracyExpression.put(objects[1], v2);
      }

      // S1 = v1 &/|| v2
      if (operator == "&") {
        // S1 = v1*v2
        qhs_accu.addTerm(1, stepAccuracyExpression.get(objects[0]), stepAccuracyExpression.get(objects[1]));
      } else {
        // S1 = v1+v2-v1*v2
        qhs_accu.addTerm(1, stepAccuracyExpression.get(objects[0]));
        qhs_accu.addTerm(1, stepAccuracyExpression.get(objects[1]));
        qhs_accu.addTerm(-1, stepAccuracyExpression.get(objects[0]), stepAccuracyExpression.get(objects[1]));
      }
      GRBVar v3 = model.addVar(0, 1, 1, GRB.CONTINUOUS, name + ".express");
      model.addQConstr(v3, GRB.EQUAL, qhs_accu, name);
      stepAccuracyExpression.put(name, v3);
    }

    // The objective is to maximize the accuracy
    GRBQuadExpr objectiveExpr = new GRBQuadExpr();
    objectiveExpr.addTerm(1, stepAccuracyExpression.get(name));
    return objectiveExpr;
  }


  public List<Object> getSteps(String str){
    String[] step = str.split(",");
    String name = step[1];
    String[] expression;
    String operator;
    if (step[0].contains("&")) {
      expression = step[0].split("&");
      operator = "&";
    }else{
      expression = step[0].split("\\|\\|");
      operator = "||";
    }
    String[] objects = {expression[0], expression[1]};
    return Arrays.asList(name, operator, objects);
  }

  protected BufferedReader readFile(String filePath) throws FileNotFoundException {
//    System.out.println(QueryCompiler.class.getClassLoader().getResource(filePath).toString());
    File file = new File(filePath);
    FileInputStream input = new FileInputStream(file);
    DataInputStream in = new DataInputStream(input);
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    return br;
  }

  public void showResults(GRBVar[][] x) throws GRBException {
    int status = model.get(GRB.IntAttr.Status);
    if (status == GRB.Status.UNBOUNDED) {
      System.out.println("The model cannot be solved "
          + "because it is unbounded");
      return;
    }
    if (status == GRB.Status.OPTIMAL) {
      System.out.println("The optimal objective is " +
          model.get(GRB.DoubleAttr.ObjVal));
//        System.out.println(Arrays.deepToString(x));
      for (int t = 0; t < nTasks; t++) {
        for (int m = 0; m < nModels; m++) {
//            System.out.println(x[t][m].get(GRB.StringAttr.VarName));
//            System.out.println(x[t][m].get(GRB.DoubleAttr.Xn));
          if (x[t][m].get(GRB.DoubleAttr.Xn) > 0.3) {
            System.out.println(x[t][m].get(GRB.StringAttr.VarName));
            System.out.println(x[t][m].get(GRB.DoubleAttr.Xn));
//            print(v1[model].varName, v1[model].x, acc_task1[model])
          }
        }
      }

      return;
    }
    if (status != GRB.Status.INF_OR_UNBD &&
        status != GRB.Status.INFEASIBLE) {
      System.out.println("Optimization was stopped with status " + status);
      return;
    }

    // Compute IIS
    System.out.println("The model is infeasible; computing IIS");
    model.computeIIS();
    System.out.println("\nThe following constraint(s) "
        + "cannot be satisfied:");
    for (GRBConstr c : model.getConstrs()) {
      if (c.get(GRB.IntAttr.IISConstr) == 1) {
        System.out.println(c.get(GRB.StringAttr.ConstrName));
      }
    }
  }

}
