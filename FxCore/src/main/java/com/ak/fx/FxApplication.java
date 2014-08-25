package com.ak.fx;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.storage.StageStorage;
import com.ak.storage.Storage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FxApplication extends Application {
  private static final String FX_CONTEXT_XML = "fx-context.xml";
  private static final String SCENE_XML = "scene.fxml";
  private static final String KEYS_PROPS = "keys";

  private final Storage<Stage> stageStorage = new StageStorage(getClass().getSimpleName());
  private final ListableBeanFactory context = new ClassPathXmlApplicationContext(
      getClass().getPackage().getName().replaceAll("\\.", "/") + "/" + FX_CONTEXT_XML
  );

  @Override
  public final void start(Stage stage) throws Exception {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(SCENE_XML),
          ResourceBundle.getBundle(getClass().getPackage().getName() + "." + KEYS_PROPS));
      loader.setControllerFactory(clazz -> BeanFactoryUtils.beanOfType(context, clazz));
      stage.setScene(new Scene(loader.load()));
      stage.setTitle(loader.getResources().getString("application.title"));
      stage.setOnCloseRequest(event -> stageStorage.save(stage));
      stageStorage.load(stage);
      stage.show();
    }
    catch (Exception e) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
      throw e;
    }
  }
}