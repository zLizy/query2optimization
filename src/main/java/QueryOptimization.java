import gurobi.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class QueryOptimization {
  private static String filePath;
  public QueryOptimization(String path){
    filePath = path;
  }

  public void optimize(String query) throws IOException, GRBException {
//    String query = "(object=car or (object=truck and color=yellow) or object=bus) and (weather=rainy or weather=windy);"
    QueryOptimization optimization = new QueryOptimization("src/main/java/Query2Steps/output.txt");

    Modeling modeling = new Modeling(query);
    modeling.gurobiOptimizer();

  }


  protected BufferedReader readFile() throws FileNotFoundException {
//    System.out.println(QueryCompiler.class.getClassLoader().getResource(filePath).toString());
    File file = new File(filePath);
    FileInputStream input = new FileInputStream(file);
    DataInputStream in = new DataInputStream(input);
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    return br;
  }

}


