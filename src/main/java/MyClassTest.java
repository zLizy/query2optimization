import Query2Steps.Query2Steps;

import gurobi.GRBException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;

public class MyClassTest {
  private static Query2Steps tester = new Query2Steps();

  @Test
  /*
    car&red,S1
    truck&yellow,S2
    S1||S2,S3
   */
  public void testParser() throws IOException, ParseException {
//    String query = "(object=car and color=red) or (object=truck and color=yellow);";
    String query = "(class=toxic and class=severe_toxic) or class=obscene or class=identity_hate;";
    String output = tester.parser(query);
    System.out.println(output);
  }

  @Test
  /*
    car&red,S1
    truck&yellow,S2
    S1||S2,S3
    rainy||windy,S4
    S3&S4,S5
   */
  public void testParser2() throws IOException, ParseException {
//    String path = "src/main/java/query.txt";
    String query = "((object=car and color=red) or (object=truck and color=yellow)) and (weather=rainy or weather=windy);";
    String output = tester.parser(query);
    System.out.println(output);
  }

  /*
  The optimal objective is 0.999
  car.model2
  1.0
  truck.model4
  1.0
  red.model6
  1.0
  yellow.model5
  0.9895833333333334
   */
  @Test
  public void testDefaultOptimization() throws IOException, ParseException, GRBException, URISyntaxException {
    String query = "(object=car and color=red) or (object=truck and color=yellow);";
    Modeling modeling = new Modeling("default",tester.parser(query));
    modeling.gurobiOptimizer();
  }

  @Test
  public void testDefaultOptimization2() throws IOException, ParseException, GRBException, URISyntaxException {
    String query = "object=car or (object=truck and color=red) or color=yellow;";
    Modeling modeling = new Modeling("default",tester.parser(query));
    modeling.gurobiOptimizer();
  }

  @Test
  public void testTextOptimization() throws IOException, ParseException, GRBException, URISyntaxException {
    String query = "(class=toxic and class=severe_toxic) or class=obscene or class=identity_hate;";
    Modeling modeling = new Modeling("text",tester.parser(query));
    modeling.gurobiOptimizer();
  }

  @Test
  public void testTextModelStatistics() throws FileNotFoundException, URISyntaxException {
    Config config = new Config();
    URL modelPath = this.getClass().getClassLoader().getResource("model"); // "model"
    Map<String,Object> dict = config.getTextStatistics(modelPath);
    double[][] accuracy = (double[][]) dict.get("Accuracy");
    System.out.println(Arrays.deepToString(accuracy));
    System.out.println(Arrays.toString((String[])dict.get("Task")));
  }

  @Test
  public void testSysthesisData(){
    Map<String,Object> dict = Config.getSysthesisData();
    double[][] accuracy = (double[][]) dict.get("Accuracy");
    System.out.println(Arrays.deepToString(accuracy));
  }


  @Test
  public void testModelPath() {
    System.out.println(MyClassTest.class.getResource("/"));
    URL modelPath = MyClassTest.class.getClassLoader().getResource("model"); // "model"
    System.out.println(modelPath.toString());
  }
  @Test
  public void testSplit(){
    String line = "a\nb";
    for (String s: line.split("\n")){
      System.out.println(s);
    }

  }


}
