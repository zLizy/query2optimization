import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Config {

//  public static String constraint = "accuracy";
  public static String constraint = "cost";
  // Sample data
  // Sets of days and workers
  public static  String Tasks[] =
      new String[] { "car", "truck", "red", "yellow"};
  public static  String Models[] =
      new String[] { "model1", "model2","model3","model4","model5","model6"};

  public static Double COSTBOUND = 160.0;
  public static Double ACCURACYBOUND = 0.90;

  public static  int nTasks = Tasks.length;//Tasks.length;
  public static int nModels = Models.length;

  public static double[] Tasks_availability = getOnes(nTasks);
  public static double[] Models_availability = getOnes(nModels);


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


  // Worker availability: 0 if the worker is unavailable for a shift
  // Cost: the processing time of a model on a task
  public static double availability[][] =
      new double[][] { { 1, 1, 0, 0,0,0},
          { 0, 0, 1, 1,0,0},
          { 1, 1, 0, 0,1,1},
          { 0, 0, 1, 1, 1,1} };
//  private static Object accuModel;

  public static Map<String,Object> getSysthesisData(){
    Map<String,Object> dict = new HashMap<>();
    dict.put("Task",new String[] { "car", "truck", "red", "yellow"});
    dict.put("Model",new String[] { "model1", "model2","model3","model4","model5","model6"});
    dict.put("Cost",new double[][] { { 1, 1, 0, 0,0,0},
        { 0, 0, 1, 1,0,0},
        { 1, 1, 0, 0,1,1},
        { 0, 0, 1, 1, 1,1} });
    dict.put("Accuracy",new double[][]{
        {0.96,0.98,0,0,0,0},
        {0,0,0.93,0.95,0,0},
        {0,0,0,0,0.96,0.98},
        {0,0,0,0,0.96,0.98}});
    return dict;
  }

  // parse model files and get accuracy and cost
  public static Map<String,Object> getTextStatistics(URL modelPath) throws URISyntaxException, FileNotFoundException {
    Map<String,Object> dict = new HashMap<>();
    List<Map<String, Double>> modelAcc = new ArrayList<Map<String, Double>>();
    List<Map<String, Double>> modelCost = new ArrayList<Map<String, Double>>();
    Map<String, Integer> taskMap = new HashMap<>();
    int count=0;

    System.out.println(modelPath.toString());
    File folder = new File(modelPath.toURI());
    File[] listOfFiles= folder.listFiles();
    System.out.println(listOfFiles.length);

    String fileName;
    List<String> models = new ArrayList<String>();
    //JSON parser object to parse read file
    JSONParser jsonParser = new JSONParser();
    for (int i = 0; i < listOfFiles.length; i++) {
      fileName = listOfFiles[i].getName();
      if (fileName.endsWith(".csv")){ continue;}
      System.out.println(fileName);
      String modelName = fileName.split("\\.")[0];
      models.add(modelName);
      try  (FileReader reader = new FileReader(listOfFiles[i]))
      {
        //Read JSON file
        Object obj = jsonParser.parse(reader);
        JSONArray performanceList = new JSONArray();
        performanceList.add(obj);
//        System.out.println(performanceList);

        //Iterate over employee array
//        performanceList.forEach( model -> parseModelObject( (JSONObject) model ) );
        for (Object model: performanceList){
          List<Object> parseResutls = parseModelObject((JSONObject) model);

          System.out.println(parseResutls.get(0));
          Map<String, Double> accuModel = (Map<String, Double>) parseResutls.get(0);
          modelAcc.add(accuModel);
          Map<String, Double> costModel = (Map<String, Double>) parseResutls.get(1);
          modelCost.add(costModel);

          for(String task: accuModel.keySet()) {
            if(taskMap.get(task) == null){
              taskMap.put(task,count++);
            }
          }
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    System.out.println(Arrays.asList(taskMap));

    String[] tasksList = taskMap.keySet().toArray(new String[0]);
    String[] modelList = models.toArray(new String[0]);
    int modelLen = modelList.length;
    int taskLen = tasksList.length;

    double[][] cost = getProperties(300,taskLen, modelLen, modelCost, taskMap);
    double[][] accuracy = getProperties(0,taskLen, modelLen, modelAcc, taskMap);

    dict.put("Model",modelList);
    dict.put("Task",tasksList);
    dict.put("Accuracy", accuracy);
    dict.put("Cost",cost);
    return dict;
  }

  private static List<Object> parseModelObject(JSONObject model) {
//    "task": {
//      "toxic": {"accuracy": 0.91, "cost": 51.26517},
//      "severe_toxic": {"accuracy": 0.992, "cost": 51.26517}

    //Get employee object within list
    JSONObject modelObject = (JSONObject) model.get("task");
    JSONObject performanceObject;
    Map<String, Double> accuModel = new HashMap<String, Double>();
    Map<String, Double> costModel = new HashMap<String, Double>();
    String acc = "accuracy";
    String cost = "cost";
    ArrayList<String> list = new ArrayList<String>(modelObject.keySet());

    for (Iterator iterator = modelObject.keySet().iterator(); iterator.hasNext(); ) {
      String key = (String) iterator.next();
      performanceObject = (JSONObject) modelObject.get(key);
      accuModel.put(key, (Double) performanceObject.get(acc));
      costModel.put(key, (Double) performanceObject.get(cost));
//      System.out.println(modelObject.get(key));
    }
    return Arrays.asList(accuModel, costModel);

    //Get model dataset
//    String dataSet = (String) modelObject.get("dataSet");

  }

  public static double[][] getProperties(int fillValue, int taskLen, int modelLen,
      List<Map<String, Double>> modelAcc, Map<String,Integer> taskMap){
    int modelCount=0;
    double [][] cost = new double[taskLen][modelLen];
    for (double[] row: cost)
      Arrays.fill(row, fillValue);
    for(Map<String, Double> entry : modelAcc){
      for (Map.Entry<String,Double> pair : entry.entrySet()){
        int id = taskMap.get(pair.getKey());
        cost[id][modelCount] = pair.getValue();
      }
      modelCount++;
    }
    return cost;
  }

  public static void print(){
    System.out.println(Arrays.asList(Tasks));
    System.out.println(Arrays.asList(Models));
    System.out.println(Arrays.deepToString(accuracy));
    System.out.println(Arrays.deepToString(cost));
  }


  public static double [] getOnes(int len) {
    double Models_availability[] = new double[len];
    Arrays.fill(Models_availability, 1);
    return Models_availability;
  }
}
